package rs.ac.uns.ftn.informatika.rest.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // Care RabbitMQ setup
    @Value("${care.rabbitmq.queue.name}")
    private String careQueueName;

    @Value("${care.rabbitmq.exchange.name}")
    private String careExchangeName;

    @Value("${care.rabbitmq.routing.key}")
    private String careRoutingKey;

    // Bean za Queue (Red)
    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    // Bean za Exchange (razmjenu)
    @Bean
    public FanoutExchange  exchange() {
        return new FanoutExchange (exchangeName);
    }

    // Care RabbitMQ - Bean za Queue
    @Bean
    public Queue careQueue() {
        return new Queue(careQueueName, true);
    }

    // Care RabbitMQ - Bean za Exchange
    @Bean
    public DirectExchange careExchange() {
        return new DirectExchange(careExchangeName);
    }

    // Bean za Binding (povezivanje reda i razmjene)
    @Bean
    public Binding binding(Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    // Care RabbitMQ - Bean za Binding
    @Bean
    public Binding careBinding(Queue careQueue, DirectExchange careExchange) {
        return BindingBuilder.bind(careQueue).to(careExchange).with(careRoutingKey);
    }
    // Zajednički Bean za konverter poruka (JSON)
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    // Prvi RabbitMQ - Bean za RabbitTemplate (koristi ga Producer)
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    // Care RabbitMQ - Bean za RabbitTemplate
    @Bean
    public AmqpTemplate careAmqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    // Prvi RabbitMQ - Bean za ListenerContainerFactory
    @Bean
    public SimpleRabbitListenerContainerFactory adRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

    // Care RabbitMQ - Bean za ListenerContainerFactory
    @Bean
    public SimpleRabbitListenerContainerFactory careRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }
}
