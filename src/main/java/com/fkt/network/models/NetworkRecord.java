package com.fkt.network.models;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
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
    private String protocol;

    @Nullable
    private String note;

    @NonNull
    private String fullNetworkRecord;
}
