package com.fkt.network.dtos.request;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkRecordAMQPRequestDTO {
    private String outputIp;
    private String outputPort;

    private String inputIp;
    private String inputPort;
    private String protocol = "TCP";


    private String note;


    private String fullNetworkRecord;
    private String operation;
}
