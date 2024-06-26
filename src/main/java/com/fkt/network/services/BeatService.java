package com.fkt.network.services;

import com.fkt.network.dtos.request.BeatAMQPRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
@ConditionalOnExpression("${spring.rabbitmq.enable}")
public class BeatService {
    private RabbitTemplate rabbitTemplate;
    private String exchange="beat";

    private String routingKey="beat";
    @Value("${host.ip}")
    private String hostIp;
    @Autowired
    public BeatService(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    @Scheduled(fixedRate = 10000)

    public void publishMessagePeriodically() {
        BeatAMQPRequestDTO dto = new BeatAMQPRequestDTO(this.hostIp);
        rabbitTemplate.convertAndSend(this.exchange, this.routingKey, dto);
        System.out.println("Sent Beat'" + dto + "'");
    }
}