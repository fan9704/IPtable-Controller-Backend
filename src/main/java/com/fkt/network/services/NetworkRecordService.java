package com.fkt.network.services;

import com.fkt.network.dtos.ExecuteCommandRequestDTO;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import com.fkt.network.models.NetworkRecord;
import com.fkt.network.repositories.NetworkRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

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

    public NetworkRecord create_nat_service(NetworkRecordCreateDTO dto){
        // Check Rule is repeat
        String fullNetworkRecord = String.format("%s:%s:%s:%s",
                dto.getOutputPort(),
                dto.getOutputIp(),
                dto.getInputPort(),
                dto.getInputIp()
        );
        Boolean repeated = this.check_repeat_record(fullNetworkRecord);
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
        if(networkRecordList.size() != 0){
            return true;
        }else{
            return false;
        }
    }
    // Execute Command
    public Boolean execute_command(String originCommand){
        String system=System.getProperty("os.name");
        boolean isWindows = system.contains("Windows");
        String[] command = null;
        if(isWindows){
            System.out.println("Windows System");
            command = ("cmd.exe /c "+originCommand).split(" ") ;
        }else{
            System.out.println("Linux System");
            command = ("sh -c "+originCommand).split(" ") ;
        }
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        String response = "";
        boolean status;

        try{
            Process process = builder.start();
            process.waitFor();
            status = true;

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = null;
            while((str=(buffer.readLine()))!=null){
                response+=str;
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
        String[] command = null;
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
        String response = "";
        HttpStatus httpStatus;
        Boolean status;
        try{
            Process process = builder.start();
            process.waitFor();
            OutputStream outputStream =process.getOutputStream();
            status = true;
            httpStatus = HttpStatus.OK;

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str = null;
            while((str=(buffer.readLine()))!=null){
                response+=str;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println(e);
            response = "Not success";
            status = false;
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        responseDTO.setResponse(response);
        responseDTO.setStatus(status);
        return new ResponseEntity<ExecuteCommandResponseDTO>(responseDTO, httpStatus);
    }


    // SSH Service
    public void ssh_macro(){

//        networkRecord.setInputPort("22");
    }
}
