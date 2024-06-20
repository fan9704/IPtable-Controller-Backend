package com.fkt.network.services;

import com.fkt.network.dtos.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.request.NetworkRecordRequestDTO;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NetworkRecordService {
    private NetworkRecordRepository repository;
    private NATService natService;
    private final RabbitTemplate rabbitTemplate;
    @Value("{spring.rabbitmq.enable}")
    private Boolean amqpIsEnable = false;
    @Autowired
    public NetworkRecordService(NetworkRecordRepository repository, NATService natService, RabbitTemplate rabbitTemplate){
        this.repository = repository;
        this.natService = natService;
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter( new Jackson2JsonMessageConverter());
    }

    public ResponseEntity<List<NetworkRecord>> findAllNetworkRecord(){
        return new ResponseEntity<>(this.repository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<NetworkRecord> create_service(NetworkRecordCreateDTO dto) {
        NetworkRecord networkRecord=this.create_nat_service(dto);
        if(networkRecord == null){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }else{
            // Send AMQP Create Message
            this.ampqSendJSON(networkRecord,"create");
            return new ResponseEntity<>(networkRecord, HttpStatus.CREATED);
        }
    }
    public NetworkRecord create_nat_service(NetworkRecordCreateDTO dto) {
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
            // Save Record to database
            NetworkRecord networkRecord = dto.dtoToNetworkRecord();
            return this.repository.save(networkRecord);
        }else{
            System.out.println("Create NAT Rules to iptables failed");
            return null;
        }
    }

    public ResponseEntity<NetworkRecord> find_network_record_by_id(String id){
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        return optionalNetworkRecord.map(networkRecord -> new ResponseEntity<>(networkRecord, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<NetworkRecord> patch_network_record_by_id(String id, NetworkRecordRequestDTO dto) throws IOException {
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
            // Send AMQP Update Message
            this.ampqSendJSON(newNetworkRecord,"update");
            return new ResponseEntity<>(this.repository.save(newNetworkRecord),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
    }

    private NetworkRecord editNetworkRecord(NetworkRecord networkRecord,NetworkRecordRequestDTO dto) {
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



    public ResponseEntity<?> delete_nat_service(String id) throws IOException {
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
            // Send AMQP Delete Message
            this.ampqSendJSON(networkRecord,"delete");
            return new ResponseEntity<>(null,HttpStatus.OK);

        }else{
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public boolean check_repeat_record(String fullNetworkRecord){
        List<NetworkRecord> networkRecordList=this.repository.findByFullNetworkRecordIs(fullNetworkRecord);
        return !networkRecordList.isEmpty();
    }

    public ResponseEntity<ExecuteCommandResponseDTO> execute_command(ExecuteCommandRequestDTO dto){
        String system=System.getProperty("os.name");
        boolean isWindows = system.contains("Windows");
        String command;
        if(isWindows){
            System.out.println("Windows System");
            command = "cmd.exe /c "+dto.getCommand() ;
        }else{
            System.out.println("Linux System");
            command = dto.getCommand() ;
        }
        System.out.println("Request Command"+command);
        ExecuteCommandResponseDTO responseDTO = new ExecuteCommandResponseDTO();
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command.split(" "));
        StringBuilder response = new StringBuilder();
        HttpStatus httpStatus;
        boolean status;
        try{
            Process process = builder.start();
            process.waitFor();
            status = true;
            httpStatus = HttpStatus.OK;

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str;
            while((str=(buffer.readLine()))!=null){
                response.append(str);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println(e);
            response = new StringBuilder("Not success");
            status = false;
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        responseDTO.setResponse(response.toString());
        responseDTO.setStatus(status);
        return new ResponseEntity<>(responseDTO, httpStatus);
    }
    public void ampqSendJSON(NetworkRecord networkRecord,String operation){
        if(this.amqpIsEnabled()){
            rabbitTemplate.convertAndSend("client","client",networkRecord.toAMQPDTO(operation));
            System.out.println("Sent AMQP Message");
        }else{
            System.out.println("AMQP Is Disabled");
        }
    }

    public Boolean amqpIsEnabled(){
        if(this.amqpIsEnable == null){
            return false;
        }else{
            return this.amqpIsEnable;
        }
    }
}
