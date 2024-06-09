package com.fkt.network.services;

import com.fkt.network.dtos.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.request.NetworkRecordRequestDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import com.fkt.network.models.NetworkRecord;
import com.fkt.network.repositories.NetworkRecordRepository;
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
    @Value("${docker.enabled:true}")
    private Boolean dockerEnabled ;
    private NetworkRecordRepository repository;
    private NATService natService;
    @Autowired
    public NetworkRecordService(NetworkRecordRepository repository,NATService natService){
        this.repository = repository;
        this.natService = natService;
    }

    public ResponseEntity<List<NetworkRecord>> findAllNetworkRecord(){
        return new ResponseEntity<>(this.repository.findAll(), HttpStatus.OK);
    }
    public ResponseEntity<NetworkRecord> create_service(NetworkRecordCreateDTO dto) throws IOException {
        NetworkRecord networkRecord=this.create_nat_service(dto);
        if(networkRecord == null){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }else{
            return new ResponseEntity<>(networkRecord, HttpStatus.CREATED);
        }
    }

    public ResponseEntity<NetworkRecord> find_network_record_by_id(String id){
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        return optionalNetworkRecord.map(networkRecord -> new ResponseEntity<>(networkRecord, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<NetworkRecord> patch_network_record_by_id(String id, NetworkRecordRequestDTO dto){
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        if(optionalNetworkRecord.isPresent()){
            NetworkRecord networkRecord=optionalNetworkRecord.get();
            // Patch Network Record
            networkRecord=this.editNetworkRecord(networkRecord,dto);
            return new ResponseEntity<>(this.repository.save(networkRecord),HttpStatus.OK);
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

        networkRecord.setFullNetworkRecord(this.getFullNetworkRecord(networkRecord));
        return networkRecord;
    }


    public NetworkRecord create_nat_service(NetworkRecordCreateDTO dto) throws IOException {
        // Check Rule is repeat
        String fullNetworkRecord = this.getFullNetworkRecord(dto);
        boolean repeated = this.check_repeat_record(fullNetworkRecord);
        if(repeated){
            return null;
        }
        // Create NAT Service in Host
        Boolean createNATStatus= this.execute_create_nat_command(dto);
        if(createNATStatus){
            // Save Record to database
            NetworkRecord networkRecord = getNetworkRecord(dto, fullNetworkRecord);
            return this.repository.save(networkRecord);
        }else{
            System.out.println("Create NAT Rules to iptables failed");
            return null;
        }
    }
    public ResponseEntity<?> delete_nat_service(String id) throws IOException {
        Optional<NetworkRecord>networkRecordOptional = this.repository.findById(id);
        if(networkRecordOptional.isPresent()){
            NetworkRecord networkRecord = networkRecordOptional.get();
            NetworkRecordCreateDTO dto = this.networkRecordToDTO(networkRecord);
            // Delete Rules
            Boolean delete_prerouting_success=this.natService.execute_delete_nat_prerouting(dto);
            Boolean delete_postrouting_success=this.natService.execute_delete_nat_postrouting(dto);


            if(delete_prerouting_success && delete_postrouting_success){
                this.repository.deleteById(id);
                return new ResponseEntity<>(null,HttpStatus.OK);
            }else{
                return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
            }
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
    public Boolean execute_create_nat_command(NetworkRecordCreateDTO dto) throws IOException {
        Boolean preRoutingSuccess;
        Boolean postRoutingSuccess;

        try{
            // PreRouting
            preRoutingSuccess=this.natService.execute_create_nat_prerouting(dto);
            // PostRouting
            if(preRoutingSuccess){
                postRoutingSuccess= this.natService.execute_create_nat_postrouting(dto);
                return postRoutingSuccess;
            }else{
                return false;
            }
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
        builder.command(command);
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
    // Utils
    private NetworkRecord getNetworkRecord(NetworkRecordCreateDTO dto, String fullNetworkRecord) {
        NetworkRecord networkRecord = new NetworkRecord();

        networkRecord.setInputIp(dto.getInputIp());
        networkRecord.setInputPort(dto.getInputPort());
        networkRecord.setOutputIp(dto.getOutputIp());
        networkRecord.setOutputPort(dto.getOutputPort());
        networkRecord.setNote(dto.getNote());
        networkRecord.setProtocol(dto.getProtocol());
        networkRecord.setFullNetworkRecord(fullNetworkRecord);
        return networkRecord;
    }
    private NetworkRecordCreateDTO networkRecordToDTO(NetworkRecord networkRecord){
        NetworkRecordCreateDTO dto = new NetworkRecordCreateDTO();
        dto.setInputIp(networkRecord.getInputIp());
        dto.setInputPort(networkRecord.getInputPort());
        dto.setOutputIp(networkRecord.getOutputIp());
        dto.setOutputPort(networkRecord.getOutputPort());
        dto.setNote(networkRecord.getNote());
        dto.setProtocol(networkRecord.getProtocol());
        return dto;
    }
    private String getFullNetworkRecord(NetworkRecord networkRecord){
        return String.format("%s:%s:%s:%s",
                networkRecord.getOutputPort(),
                networkRecord.getOutputIp(),
                networkRecord.getInputPort(),
                networkRecord.getInputIp()
        );
    }
    private String getFullNetworkRecord(NetworkRecordCreateDTO dto){
        return String.format("%s:%s:%s:%s",
                dto.getOutputPort(),
                dto.getOutputIp(),
                dto.getInputPort(),
                dto.getInputIp()
        );
    }

    public String replaceIptablesToLegacy(String input) {
        if (this.dockerEnabled) {
            return input.replaceAll("iptables", "iptables-legacy");
        } else {
            return input;
        }
    }
}
