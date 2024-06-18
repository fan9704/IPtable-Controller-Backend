package com.fkt.network.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeatAMQPRequestDTO {
    private String hostIp;
    private String backendUrl;
    private String frontendUrl;
    private String note;

    public BeatAMQPRequestDTO(String hostIp){
        this.backendUrl = "http://"+hostIp+":9990";
        this.frontendUrl = "http://"+hostIp;
        this.hostIp = hostIp;
    }
}
