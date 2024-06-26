package com.fkt.network.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fkt.network.dtos.request.NetworkRecordRequestDTO;
import com.fkt.network.models.NetworkRecord;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
@ConditionalOnExpression("${spring.rabbitmq.enable}")
public class NATQueueClientService {
    @Value("${host.ip}")
    private String hostIp;
    private NetworkRecordService service;
    @Autowired
    NATQueueClientService(NetworkRecordService service){
        this.service = service;
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "nat.master",durable = "false",autoDelete = "true"),
            exchange = @Exchange(value = "master"),
            key = "master"
    ))
    public void listenBeat(String message) {
        System.out.println("Consuming Message - " + new String(message.getBytes()));
        try {
            NetworkRecordRequestDTO dto =this.messageToRequestDTO(new String(message.getBytes()));
            if(Objects.equals(dto.getOutputIp(), this.hostIp)){
                System.out.println("Operation"+dto.getOperation());
                if(Objects.equals(dto.getOperation(), "create")){
                    this.createNetworkRecordByAMQPAdapter(dto);
                }else if(Objects.equals(dto.getOperation(), "update")){
                    this.updateNetworkRecordByAMQPAdapter(dto);
                }else if(Objects.equals(dto.getOperation(),"delete")){
                    this.deleteNetworkRecordByAMQPAdapter(dto);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void createNetworkRecordByAMQPAdapter(NetworkRecordRequestDTO dto){
        NetworkRecord networkRecord = this.service.findNetworkRecordByFullNetworkRecord(dto.getFullNetworkRecord());
        if(networkRecord != null){
            this.service.createNatService(dto.toNetworkRecordCreateDTO());
        }
    }

    public void updateNetworkRecordByAMQPAdapter(NetworkRecordRequestDTO dto) throws IOException {
        NetworkRecord networkRecord = this.service.findNetworkRecordByFullNetworkRecord(dto.getFullNetworkRecord());
        if(networkRecord != null){
            this.service.patchNetworkRecordById(networkRecord.getId(),dto.toNetworkRecordCreateDTO());
        }
    }
    public void deleteNetworkRecordByAMQPAdapter(NetworkRecordRequestDTO dto){
        this.service.deleteNetworkRecordByFullNetworkRecord(dto.getFullNetworkRecord());
    }
    public NetworkRecordRequestDTO messageToRequestDTO(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        NetworkRecordRequestDTO dto = objectMapper.readValue(message, NetworkRecordRequestDTO.class);
        return dto;
    }
}
