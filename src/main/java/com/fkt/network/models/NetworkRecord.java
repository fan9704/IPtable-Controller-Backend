package com.fkt.network.models;


import com.fkt.network.dtos.NetworkRecordCreateDTO;
import com.fkt.network.dtos.request.NetworkRecordAMQPRequestDTO;
import  lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.lang.Nullable;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NetworkRecord {
    @Id
    private String id;
    @NonNull
    private String outputIp;
    @NonNull
    private String outputPort;

    @NonNull
    private String inputIp;
    @NonNull
    private String inputPort;
    @NonNull
    @Field
    private String protocol = "TCP";

    @Nullable
    private String note;

    @NonNull
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
    public NetworkRecordCreateDTO networkRecordToDTO(){
        NetworkRecordCreateDTO dto = new NetworkRecordCreateDTO(
                getOutputIp(),
                getOutputPort(),
                getInputIp(),
                getInputPort(),
                getProtocol(),
                getNote()
        );
        return dto;
    }
    public NetworkRecordAMQPRequestDTO toAMQPDTO(String operation){
        NetworkRecordAMQPRequestDTO dto = new NetworkRecordAMQPRequestDTO(
                getOutputIp(),
                getOutputPort(),
                getInputIp(),
                getInputPort(),
                getProtocol(),
                getNote(),
                getFullNetworkRecord(),
                operation
        );
        return dto;
    }
}
