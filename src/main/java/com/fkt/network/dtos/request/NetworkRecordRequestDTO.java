package com.fkt.network.dtos.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NetworkRecordRequestDTO {
    private String outputIp;
    private String outputPort;

    private String inputIp;
    private String inputPort;

    private String protocol;
    private String note;
}
