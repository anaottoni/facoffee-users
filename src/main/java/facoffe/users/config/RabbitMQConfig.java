package facoffe.users.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue userDeactivatedQueue() {
        return new Queue("users.deactivated", true);
    }
}