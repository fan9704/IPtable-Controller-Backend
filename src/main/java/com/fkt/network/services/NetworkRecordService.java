package com.fkt.network.services;

import com.fkt.network.dtos.request.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.request.NetworkRecordCreateDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import com.fkt.network.models.NetworkRecord;
import com.fkt.network.repositories.NetworkRecordRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NetworkRecordService {
    private NetworkRecordRepository repository;
    private NATService natService;
    private CommandToolService commandToolService;
    private final RabbitTemplate rabbitTemplate;
    @Value("${spring.rabbitmq.enable}")
    private Boolean amqpIsEnable = false;
    @Autowired
    public NetworkRecordService(NetworkRecordRepository repository, NATService natService,CommandToolService commandToolService, RabbitTemplate rabbitTemplate){
        this.repository = repository;
        this.natService = natService;
        this.commandToolService = commandToolService;
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter( new Jackson2JsonMessageConverter());
    }

    public ResponseEntity<List<NetworkRecord>> findAllNetworkRecord(){
        return new ResponseEntity<>(this.repository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<NetworkRecord> createWithResponseEntity(NetworkRecordCreateDTO dto) {
        NetworkRecord networkRecord=this.createNatService(dto);
        if(networkRecord == null){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }else{
            // Send AMQP Create Message
            this.amqpSendJSON(networkRecord,"create");
            return new ResponseEntity<>(networkRecord, HttpStatus.CREATED);
        }
    }
    public NetworkRecord createNatService(NetworkRecordCreateDTO dto) {
        // Check Rule is repeat
        String fullNetworkRecord = dto.getFullNetworkRecord();
        Boolean createNATStatus;
        boolean repeated = this.check_repeat_record(fullNetworkRecord);
        if(repeated){
            System.out.println("Network Record Repeat");
            return null;
        }

        if(Objects.equals(dto.getProtocol(), "SSH")){
            // Type SSH
            createNATStatus = this.execute_ssh_nat_command(dto);
        }else{
            // Type TCP NAT
            createNATStatus = this.execute_nat_command(dto);
        }
        // Create NAT Record in Host
        if(createNATStatus){
            return this.repository.save(dto.dtoToNetworkRecord());
        }else{
            System.out.println("Create NAT Rules to iptables failed");
            return null;
        }
    }

    public ResponseEntity<NetworkRecord> findNetworkRecordById(String id){
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        return optionalNetworkRecord.map(networkRecord -> new ResponseEntity<>(networkRecord, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }
    public ResponseEntity<NetworkRecord> patchNetworkRecordByIdWithResponseEntity(String id,NetworkRecordCreateDTO dto) throws IOException {
        NetworkRecord networkRecord = this.patchNetworkRecordById(id,dto);
        if(networkRecord != null){
            // Send AMQP Update Message
            this.amqpSendJSON(networkRecord,"update");
            return new ResponseEntity<>(networkRecord,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
    }

    public NetworkRecord patchNetworkRecordById(String id, NetworkRecordCreateDTO dto) throws IOException {
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        Boolean deleteSuccess;
        Boolean createSuccess;
        // Patch Network Record
        if(optionalNetworkRecord.isPresent()){
            // Old Record and DTO
            NetworkRecord oldNetworkRecord = optionalNetworkRecord.get();
            NetworkRecordCreateDTO oldNetworkRecordDTO=oldNetworkRecord.networkRecordToDTO();
            // New Record and DTO
            NetworkRecord newNetworkRecord=this.editNetworkRecord(oldNetworkRecord,dto);
            NetworkRecordCreateDTO newNetworkRecordDTO=newNetworkRecord.networkRecordToDTO();
            // Remove Old Record
            if(oldNetworkRecord.getProtocol().equals("SSH")){
                deleteSuccess=this.natService.execute_ssh_nat_forward(oldNetworkRecordDTO,false);
            }else{
                deleteSuccess=this.natService.execute_nat_prerouting(newNetworkRecordDTO,false) && this.natService.execute_nat_postrouting(oldNetworkRecordDTO,false);
            }
            System.out.println("Delete Old Record Status:"+deleteSuccess);
            // Create New Record
            if(newNetworkRecord.getProtocol().equals("SSH")){
                createSuccess=this.natService.execute_ssh_nat_forward(oldNetworkRecordDTO,true);
            }else{
                createSuccess=this.natService.execute_nat_prerouting(newNetworkRecordDTO,true) && this.natService.execute_nat_postrouting(newNetworkRecordDTO,true);
            }
            System.out.println("Create New Record Status:"+createSuccess);

            return this.repository.save(newNetworkRecord);
        }else{
            return null;
        }
    }

    private NetworkRecord editNetworkRecord(NetworkRecord networkRecord,NetworkRecordCreateDTO dto) {
        if(!Objects.equals(dto.getInputIp(), "")){
            networkRecord.setInputIp(dto.getInputIp());
        }
        if(!Objects.equals(dto.getInputPort(), "")){
            networkRecord.setInputPort(dto.getInputPort());
        }
        if (!Objects.equals(dto.getOutputPort(), "")) {
            networkRecord.setOutputIp(dto.getOutputIp());
        }
        if (!Objects.equals(dto.getOutputPort(), "")) {
            networkRecord.setOutputPort(dto.getOutputPort());
        }
        if (!Objects.equals(dto.getNote(), "")){
            networkRecord.setNote(dto.getNote());
        }
        if (!Objects.equals(dto.getProtocol(), "")){
            networkRecord.setProtocol(dto.getProtocol());
        }

        networkRecord.setFullNetworkRecord(networkRecord.getFullNetworkRecord());
        return networkRecord;
    }

    public ResponseEntity<?> deleteNetworkRecordByIdWithResponseEntity(String id) throws IOException{
        NetworkRecord networkRecord = this.deleteNatService(id);
        if(networkRecord != null){
            // Send AMQP Delete Message
            this.amqpSendJSON(networkRecord,"delete");
            return new ResponseEntity<>(null,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    public NetworkRecord deleteNatService(String id) throws IOException {
        Boolean delete_prerouting_success;
        Boolean delete_postrouting_success;
        Optional<NetworkRecord>networkRecordOptional = this.repository.findById(id);
        if(networkRecordOptional.isPresent()){
            NetworkRecord networkRecord = networkRecordOptional.get();
            NetworkRecordCreateDTO dto = networkRecord.networkRecordToDTO();
            if(Objects.equals(dto.getProtocol(), "SSH")){
                // Type SSH
                delete_prerouting_success= this.natService.execute_ssh_nat_forward(dto,false);
                System.out.println("Delete SSH PREROUTEING:"+delete_prerouting_success);
            }else{
                // Type TCP
                delete_prerouting_success=this.natService.execute_nat_prerouting(dto,false);
                delete_postrouting_success=this.natService.execute_nat_postrouting(dto,false);
                System.out.println("Delete TCP PREROUTEING:"+delete_prerouting_success);
                System.out.println("Delete TCP POSTROUTEING:"+delete_postrouting_success);
            }
            this.repository.deleteById(id);
            return networkRecord;
        }else{
            return null;
        }
    }
    public ResponseEntity<NetworkRecord> refreshNetworkRecordById(String id){
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        if(optionalNetworkRecord.isPresent()){
            this.amqpSendJSON(optionalNetworkRecord.get(),"create");
            return new ResponseEntity<>(optionalNetworkRecord.get(),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }

    }
    public ResponseEntity<List<NetworkRecord>> refreshAllNetworkRecord(){
        List<NetworkRecord> networkRecordList=this.repository.findAll();
        for(NetworkRecord networkRecord:networkRecordList){
            this.amqpSendJSON(networkRecord,"create");
        }
        return new ResponseEntity<>(networkRecordList,HttpStatus.OK);
    }


    public ResponseEntity<ExecuteCommandResponseDTO> findAllIptablesRules(){
        ExecuteCommandResponseDTO responseDTO;
        responseDTO = this.natService.findAllIptablesRules();
        if(responseDTO.getStatus()){
            return new ResponseEntity<>(responseDTO,HttpStatus.OK);
        }else{
            return new ResponseEntity<>(responseDTO,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public Boolean execute_ssh_nat_command(NetworkRecordCreateDTO dto) {
        // PreRouting
        Boolean createPreRoutingSuccess=this.natService.execute_ssh_nat_forward(dto,true);
        System.out.println("Create PREROUTEING:"+createPreRoutingSuccess);
        return createPreRoutingSuccess ;
    }
    public Boolean execute_nat_command(NetworkRecordCreateDTO dto) {
        try{
            // PreRouting
            Boolean createPreRoutingSuccess=this.natService.execute_nat_prerouting(dto,true);
            // PostRouting
            Boolean createPostRoutingSuccess= this.natService.execute_nat_postrouting(dto,true);
            System.out.println("Create PREROUTEING:"+createPreRoutingSuccess);
            System.out.println("Create POSTROUTEING:"+createPostRoutingSuccess);
            return createPreRoutingSuccess && createPostRoutingSuccess;
        }catch (IOException e){
            return false;
        }
    }
    public NetworkRecord findNetworkRecordByFullNetworkRecord(String fullNetworkRecord){
        return this.repository.findByFullNetworkRecordIs(fullNetworkRecord).get(0);
    }
    public void deleteNetworkRecordByFullNetworkRecord(String fullNetworkRecord){
        this.repository.deleteByFullNetworkRecord(fullNetworkRecord);
    }
    public boolean check_repeat_record(String fullNetworkRecord){
        List<NetworkRecord> networkRecordList=this.repository.findByFullNetworkRecordIs(fullNetworkRecord);
        return !networkRecordList.isEmpty();
    }

    public ResponseEntity<ExecuteCommandResponseDTO> executeCommand(ExecuteCommandRequestDTO dto){

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(dto.getCommand().split(" "));
            ExecuteCommandResponseDTO responseDTO = this.commandToolService.runIptablesCommandWithResponseDTO(processBuilder);
            return new ResponseEntity<>(responseDTO,HttpStatus.OK);
        } catch (Exception e) {
            ExecuteCommandResponseDTO responseDTO = new ExecuteCommandResponseDTO();
            responseDTO.setStatus(false);
            responseDTO.setResponse(e.getMessage());
            responseDTO.setMessage("Unexpected Error Occurred");
            return new ResponseEntity<>(responseDTO,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public void amqpSendJSON(NetworkRecord networkRecord,String operation){
        if(this.amqpIsEnabled()){
            rabbitTemplate.convertAndSend("client","client",networkRecord.toAMQPDTO(operation));
            System.out.println("Sent AMQP Message");
        }else{
            System.out.println("AMQP Is Disabled");
        }
    }

    public Boolean amqpIsEnabled(){
        return this.amqpIsEnable == true;
    }
}
