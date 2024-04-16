package com.fkt.network.services;

import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class NATService {
    // List
    public ExecuteCommandResponseDTO findAllIptablesRules(){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("iptables-legacy", "-t", "nat", "-L");
            ExecuteCommandResponseDTO responseDTO = this.runIptablesCommandWithResponseDTO(processBuilder);
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
            return this.runIptablesCommand(processBuilder);
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
            return this.runIptablesCommand(processBuilder);
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
            return this.runIptablesCommand(processBuilder);
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
            return this.runIptablesCommand(processBuilder);
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
            return this.runIptablesCommand(processBuilder);
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
            return this.runIptablesCommand(processBuilder);
        }catch (IOException | InterruptedException e){
            return false;
        }
    }

    // Utils
    public Boolean runIptablesCommand(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        return exitCode == 0;
    }
    public ExecuteCommandResponseDTO runIptablesCommandWithResponseDTO(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        ExecuteCommandResponseDTO responseDTO = new ExecuteCommandResponseDTO();

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder commandResponse = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            commandResponse.append(line).append("\n");
        }
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        responseDTO.setStatus(exitCode == 0);
        responseDTO.setMessage("Complete Execute Command in ExitCode" + exitCode);
        responseDTO.setResponse(commandResponse.toString());
        return responseDTO;
    }
    public String isAppendOrDeleteRules(Boolean is_append){
        return is_append ? "-A" : "-D";
    }
}
