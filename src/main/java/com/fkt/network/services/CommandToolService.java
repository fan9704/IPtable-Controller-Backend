package com.fkt.network.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CommandToolService {
    public String execute_command(String originCommand){
        String commandResponse;
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

        try{
            Process process = builder.start();
            process.waitFor();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str;
            while((str=(buffer.readLine()))!=null){
                response.append(str);
            }
            commandResponse=response.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            commandResponse=e.toString();
        }
        return commandResponse;
    }
}
