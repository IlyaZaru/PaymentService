package kafkagoal.paymentservice.service;

import kafka_goal.payment_service.model.Payment;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.UUID;

public interface KafkaSenderService {

    Mono<SenderResult<UUID>> sendToKafkaByReactiveProducerTemplate(Payment payment);
    Mono<SenderResult<UUID>> sendToKafkaByReactiveSender(Payment payment);
}
