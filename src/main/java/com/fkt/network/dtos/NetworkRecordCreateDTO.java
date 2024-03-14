package com.fkt.network.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NetworkRecordCreateDTO {
    private String outputIp;
    private String outputPort;

    private String inputIp;
    private String inputPort;

    private String protocol;
    private String note;
}
