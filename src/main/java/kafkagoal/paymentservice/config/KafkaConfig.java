package kafkagoal.paymentservice.config;

import kafka_goal.payment_service.model.Payment;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.UUID;


@Configuration
public class KafkaConfig {

    //Создаем kafka продюсер типа KafkaSender, kafkaProperties берется из application.yml
    @Bean
    public KafkaSender<UUID, Payment> reactiveKafkaSender(KafkaProperties kafkaProperties) {
        SenderOptions<UUID, Payment> senderOptions = SenderOptions.create(kafkaProperties.buildProducerProperties());
        return KafkaSender.create(senderOptions);
    }

    //Создаем kafka продюсер типа ReactiveKafkaProducerTemplate, но по сути своей это обертка KafkaSender
    @Bean
    public ReactiveKafkaProducerTemplate<UUID, Payment> reactiveKafkaProducerTemplate(KafkaProperties kafkaProperties) {
        SenderOptions<UUID, Payment> senderOptions = SenderOptions.create(kafkaProperties.buildProducerProperties());
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }
}
