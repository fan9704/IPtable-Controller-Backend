package com.fkt.network.configs;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnExpression("${spring.rabbitmq.enable}")
public class RabbitMQConfig {

    String queueName = "nat.client";

    @Value("${spring.rabbitmq.username}")
    String username;

    @Value("${spring.rabbitmq.password}")
    private String password;
    private String exchange="client";

    private String routingJsonKey ="client";
    @Bean
    Queue queue() {
        return new Queue(this.queueName, false,false,true);
    }
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(this.exchange);
    }
    @Bean
    public Binding jsonBinding(){
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingJsonKey);
    }
    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }
}