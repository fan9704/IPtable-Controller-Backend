package com.fkt.network.services;

import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NATService {
    private CommandToolService service;
    @Autowired
    NATService(CommandToolService service){
        this.service = service;
    }
    // List
    public ExecuteCommandResponseDTO findAllIptablesRules(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("iptables-legacy", "-t", "nat", "-L");
            ExecuteCommandResponseDTO responseDTO = this.service.runIptablesCommandWithResponseDTO(processBuilder);
            return responseDTO;
        } catch (Exception e) {
            ExecuteCommandResponseDTO responseDTO = new ExecuteCommandResponseDTO();
            responseDTO.setStatus(false);
            responseDTO.setResponse(e.getMessage());
            responseDTO.setMessage("Unexpected Error Occurred");
            return responseDTO;
        }
    }

    // Create
    public Boolean execute_create_nat_postrouting(NetworkRecordCreateDTO dto) throws IOException{
        String outputIp = dto.getOutputIp();
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        try{
            // PostRouting
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "iptables-legacy","-t","nat","-A","POSTROUTING","-p","tcp","--dport",inputIp,"--dport",inputPort,"j","SNAT","--to-source",outputIp);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }
    public Boolean execute_create_nat_prerouting(NetworkRecordCreateDTO dto) throws IOException{
        String outputPort = dto.getOutputPort();
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        try{
            // PreRouting
            String inputEndpoint = inputIp+":"+inputPort;
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "iptables-legacy","-t","nat","-A","PREROUTING","-p","tcp","--dport",outputPort,"-j","DNAT","--to-destination",inputEndpoint);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    public Boolean execute_create_nat_forward(NetworkRecordCreateDTO dto) throws IOException{
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        try{
            // Forward
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "iptables-legacy","-A","FORWARD","-p","tcp","-d",inputIp,"--dport",inputPort,"-j","ACCEPT");
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }
    // Delete
    public Boolean execute_delete_nat_postrouting(NetworkRecordCreateDTO dto) throws IOException{
        String outputIp = dto.getOutputIp();
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        try{
            // PostRouting
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "iptables-legacy","-t","nat","-D","POSTROUTING","-p","tcp","--dport",inputIp,"--dport",inputPort,"j","SNAT","--to-source",outputIp);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }
    public Boolean execute_delete_nat_prerouting(NetworkRecordCreateDTO dto) throws IOException{
        String outputPort = dto.getOutputPort();
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        try{
            // PreRouting
            String inputEndpoint = inputIp+":"+inputPort;
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "iptables-legacy","-t","nat","-D","PREROUTING","-p","tcp","--dport",outputPort,"-j","DNAT","--to-destination",inputEndpoint);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    public Boolean execute_delete_nat_forward(NetworkRecordCreateDTO dto) throws IOException{
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        try{
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "iptables-legacy","-D","FORWARD","-p","tcp","-d",inputIp,"--dport",inputPort,"-j","ACCEPT");
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    public String isAppendOrDeleteRules(Boolean is_append){
        return is_append ? "-A" : "-D";
    }
}
