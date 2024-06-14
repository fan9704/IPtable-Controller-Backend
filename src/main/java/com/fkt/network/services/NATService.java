package com.fkt.network.services;

import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NATService {
    private CommandToolService service;
    @Value("${iptables.legacy}")
    private Boolean legacy=false;
    @Autowired
    NATService(CommandToolService service){
        this.service = service;
    }
    // List
    public ExecuteCommandResponseDTO findAllIptablesRules(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(this.isLegacyIptables(), "-t", "nat", "-L");
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
    public Boolean execute_nat_postrouting(NetworkRecordCreateDTO dto,Boolean isCreate) throws IOException{
        String outputIp = dto.getOutputIp();
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        String operate = this.isAppendOrDeleteRules(isCreate);
        try{
            // PostRouting
            ProcessBuilder processBuilder = new ProcessBuilder(
                    this.isLegacyIptables(),"-t","nat",operate,"POSTROUTING","-p","tcp","-d",inputIp,"--dport",inputPort,"-j","SNAT","--to-source",outputIp);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }
    public Boolean execute_nat_prerouting(NetworkRecordCreateDTO dto,Boolean isCreate) throws IOException{
        String outputPort = dto.getOutputPort();
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        String operate = this.isAppendOrDeleteRules(isCreate);
        try{
            // PreRouting
            String inputEndpoint = inputIp+":"+inputPort;
            ProcessBuilder processBuilder = new ProcessBuilder(
                    this.isLegacyIptables(),"-t","nat",operate,"PREROUTING","-p","tcp","--dport",outputPort,"-j","DNAT","--to-destination",inputEndpoint);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    public Boolean execute_nat_forward(NetworkRecordCreateDTO dto,Boolean isCreate) {
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        String operate = this.isAppendOrDeleteRules(isCreate);
        try{
            // Forward
            ProcessBuilder processBuilder = new ProcessBuilder(
                    this.isLegacyIptables(),operate,"FORWARD","-p","tcp","-d",inputIp,"--dport",inputPort,"-j","ACCEPT");
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    public Boolean execute_ssh_nat_forward(NetworkRecordCreateDTO dto,Boolean isCreate){
        String inputIp = dto.getInputIp();
        String inputPort = dto.getInputPort();
        String inputEndpoint = inputIp+":"+inputPort;
        String outputPort = dto.getOutputPort();
        String operate = this.isAppendOrDeleteRules(isCreate);
        try{
            // Forward
            ProcessBuilder processBuilder = new ProcessBuilder(
                    this.isLegacyIptables(),"-t","nat",operate,"PREROUTING","-p","tcp","--dport",outputPort,"-j","DNAT","--to",inputEndpoint);
            return this.service.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    public String isAppendOrDeleteRules(Boolean is_append){
        return is_append ? "-A" : "-D";
    }
    public String isLegacyIptables(){
        String mode = this.legacy? "iptables-legacy" : "iptables";
        System.out.println("Mode:"+ mode);
        return mode;
    }
}
