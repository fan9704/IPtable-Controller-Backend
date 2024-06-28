package com.fkt.network.dtos.request;

import com.fkt.network.models.NetworkRecord;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class   NetworkRecordCreateDTO {
    private String outputIp;
    private String outputPort;

    private String inputIp;
    private String inputPort;

    private String protocol;
    private String note;

    public  String getFullNetworkRecord(){
        return String.format("%s:%s:%s:%s",
                this.getOutputPort(),
                this.getOutputIp(),
                this.getInputPort(),
                this.getInputIp()
        );
    }
    public NetworkRecord dtoToNetworkRecord(){
        NetworkRecord networkRecord = new NetworkRecord();

        networkRecord.setInputIp(this.getInputIp());
        networkRecord.setInputPort(this.getInputPort());
        networkRecord.setOutputIp(this.getOutputIp());
        networkRecord.setOutputPort(this.getOutputPort());
        networkRecord.setNote(this.getNote());
        networkRecord.setProtocol(this.getProtocol());
        networkRecord.setFullNetworkRecord(this.getFullNetworkRecord());
        return networkRecord;
    }
}
