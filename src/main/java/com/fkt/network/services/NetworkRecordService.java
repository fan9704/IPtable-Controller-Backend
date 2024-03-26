package com.fkt.network.services;

import com.fkt.network.dtos.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.request.NetworkRecordRequestDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import com.fkt.network.models.NetworkRecord;
import com.fkt.network.repositories.NetworkRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    public NetworkRecordService(NetworkRecordRepository repository){
        this.repository = repository;
    }

    public ResponseEntity<List<NetworkRecord>> findAllNetworkRecord(){
        return new ResponseEntity<>(this.repository.findAll(), HttpStatus.OK);
    }
    public ResponseEntity<NetworkRecord> create_service(NetworkRecordCreateDTO dto){
        if(this.create_nat_service(dto) == null){
            return new ResponseEntity<>(this.create_nat_service(dto), HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(this.create_nat_service(dto), HttpStatus.CREATED);
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
            networkRecord=this.editNetworkRecord(networkRecord,dto);
            return new ResponseEntity<>(this.repository.save(networkRecord),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
    }
    public ResponseEntity delete_network_record_by_id(String id){
        Optional<NetworkRecord> optionalNetworkRecord =this.repository.findById(id);
        if(optionalNetworkRecord.isPresent()){
            this.repository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Utils API
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
    private String getFullNetworkRecord(NetworkRecord networkRecord){
        return String.format("%s:%s:%s:%s",
                networkRecord.getOutputPort(),
                networkRecord.getOutputIp(),
                networkRecord.getInputPort(),
                networkRecord.getInputIp()
        );
    }

    public NetworkRecord create_nat_service(NetworkRecordCreateDTO dto){
        // Check Rule is repeat
        String fullNetworkRecord = String.format("%s:%s:%s:%s",
                dto.getOutputPort(),
                dto.getOutputIp(),
                dto.getInputPort(),
                dto.getInputIp()
        );
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
            return null;
        }
    }

    private static NetworkRecord getNetworkRecord(NetworkRecordCreateDTO dto, String fullNetworkRecord) {
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

    public Boolean execute_create_nat_command(NetworkRecordCreateDTO dto){
        String preRoutingCommand= String.format(
                "iptables -t nat -A PREROUTING -p tcp --dport %s -j DNAT --to-destination %s:%s",
                dto.getOutputPort(),
                dto.getInputIp(),
                dto.getInputPort()
        );
        System.out.println(preRoutingCommand);
        Boolean preRoutingCommandStatus =this.execute_command(preRoutingCommand);
        if(!preRoutingCommandStatus){
            return false;
        }
        String forwardCommand= String.format(
                "iptables -A FORWARD -p tcp -d %s --dport %s -j ACCEPT",
                dto.getInputIp(),
                dto.getInputPort()
        );
        System.out.println(forwardCommand);
        Boolean forwardCommandStatus = this.execute_command(forwardCommand);
        if(!forwardCommandStatus){
            String deletePreRoutingCommand = String.format(
                    "iptables -t nat -D PREROUTING -p tcp --dport %s -j DNAT --to-destination %s:%s",
                    dto.getOutputPort(),
                    dto.getInputIp(),
                    dto.getInputPort()
            );
            this.execute_command(
                deletePreRoutingCommand
            );
            return false;
        }

        return true;

    }
    public boolean check_repeat_record(String fullNetworkRecord){
        List<NetworkRecord> networkRecordList=this.repository.findByFullNetworkRecordIs(fullNetworkRecord);
        return !networkRecordList.isEmpty();
    }
    // Execute Command
    public Boolean execute_command(String originCommand){
        String system=System.getProperty("os.name");
        boolean isWindows = system.contains("Windows");
        String[] command;
        if(isWindows){
            System.out.println("Windows System");
            command = ("cmd.exe /c "+originCommand).split(" ") ;
        }else{
            System.out.println("Linux System");
            command = ("sh -c "+originCommand).split(" ") ;
        }
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        StringBuilder response = new StringBuilder();
        boolean status;

        try{
            Process process = builder.start();
            process.waitFor();
            status = true;

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str;
            while((str=(buffer.readLine()))!=null){
                response.append(str);
            }
            System.out.println(response);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println(e);
            status = false;
        }
        return status;
    }
    public ResponseEntity<ExecuteCommandResponseDTO> execute_command(ExecuteCommandRequestDTO dto){
        String system=System.getProperty("os.name");
        boolean isWindows = system.contains("Windows");
        String[] command;
        if(isWindows){
            System.out.println("Windows System");
            command = ("cmd.exe /c "+dto.getCommand()).split(" ") ;
        }else{
            System.out.println("Linux System");
            command = ("sh -c "+dto.getCommand()).split(" ") ;
        }
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


    // SSH Service
    public void ssh_macro(){

//        networkRecord.setInputPort("22");
    }
}
