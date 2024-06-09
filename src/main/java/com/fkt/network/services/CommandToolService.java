package com.fkt.network.services;

import com.fkt.network.dtos.response.ExecuteCommandResponseDTO;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CommandToolService {
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
}
