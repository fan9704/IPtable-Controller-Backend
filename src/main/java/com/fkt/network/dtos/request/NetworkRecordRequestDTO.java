package com.fkt.network.dtos.request;
import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.models.NetworkRecord;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NetworkRecordRequestDTO {
    private String id;
    private String operation;
    private String outputIp;
    private String outputPort;
    private String inputIp;
    private String inputPort;
    private String protocol = "TCP";
    private String note;
    private String fullNetworkRecord;

    public void generateFullNetworkRecord(){
        this.setFullNetworkRecord(String.format("%s:%s:%s:%s",
                this.getOutputPort(),
                this.getOutputIp(),
                this.getInputPort(),
                this.getInputIp()
        ));
    }
    public String getFullNetworkRecord(){
        return String.format("%s:%s:%s:%s",
                this.getOutputPort(),
                this.getOutputIp(),
                this.getInputPort(),
                this.getInputIp()
        );
    }
    public NetworkRecordCreateDTO toNetworkRecordCreateDTO(){
        NetworkRecordCreateDTO dto = new NetworkRecordCreateDTO();
        dto.setProtocol(this.getProtocol());
        dto.setInputIp(this.getInputIp());
        dto.setInputPort(this.getInputPort());
        dto.setOutputIp(this.getOutputIp());
        dto.setOutputPort(this.getOutputPort());
        dto.setNote(this.getNote());
        return dto;
    }
    public NetworkRecord toNetworkRecord(){
        NetworkRecord networkRecord= new NetworkRecord();
        networkRecord.setNote(this.getNote());
        networkRecord.setFullNetworkRecord(this.getFullNetworkRecord());
        networkRecord.setProtocol(this.getProtocol());
        networkRecord.setInputIp(this.getInputIp());
        networkRecord.setInputPort(this.getInputPort());
        networkRecord.setOutputIp(this.getOutputIp());
        networkRecord.setOutputPort(this.getOutputPort());
        return networkRecord;
    }
}
