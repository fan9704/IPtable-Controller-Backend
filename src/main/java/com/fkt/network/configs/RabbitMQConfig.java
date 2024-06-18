package com.fkt.network.configs;


import com.fkt.network.services.NATQueueClientService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnExpression("${spring.rabbitmq.enable}")
public class RabbitMQConfig {

    @Value("${fkt.rabbitmq.receive.queue}")
    String queueName;

    @Value("${spring.rabbitmq.username}")
    String username;

    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.exchange.name:client}")
    private String exchange;

    @Value("${rabbitmq.routing.json.key:140.96.83.14}")
    private String routingJsonKey;
    @Bean
    Queue queue() {
        return new Queue(this.queueName, false);
    }
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(this.exchange);
    }
    //TODO:Auto Bind
    @Bean
    public Binding jsonBinding(){
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingJsonKey);
    }
    //create MessageListenerContainer using default connection factory
    @Bean
    MessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory ) {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueues(queue());
        simpleMessageListenerContainer.setMessageListener(new NATQueueClientService());
        return simpleMessageListenerContainer;

    }
    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}