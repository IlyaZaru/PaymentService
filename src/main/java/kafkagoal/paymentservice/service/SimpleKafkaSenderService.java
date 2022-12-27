package kafkagoal.paymentservice.service;

import kafka_goal.payment_service.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.sql.Timestamp;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleKafkaSenderService implements KafkaSenderService {
    private final KafkaSender<UUID, Payment> kafkaSender;
    private final ReactiveKafkaProducerTemplate<UUID, Payment> reactiveKafkaProducerTemplate;
    @Value("${kafka.payment-topic}")
    private String topic;

    @Override
    public Mono<SenderResult<UUID>> sendToKafkaByReactiveProducerTemplate(Payment payment) {
        var correlationId = payment.getRequestId();
        return reactiveKafkaProducerTemplate.send(convertToSenderRecord(payment))
                .doOnSuccess(senderResult -> log.info("sendToKafkaByReactiveProducerTemplate(): " + logSuccessResult(senderResult)))
                .doOnError(throwable -> log.error("sendToKafkaByReactiveProducerTemplate(): Error of dispatch throwable = {}", throwable.getMessage()))
                .filter(senderResult -> correlationId.equals(senderResult.correlationMetadata()));
    }

    @Override
    public Mono<SenderResult<UUID>> sendToKafkaByReactiveSender(Payment payment) {
        var correlationId = payment.getRequestId();
        return kafkaSender.send(Mono.just(convertToSenderRecord(payment)))
                .doOnNext(senderResult -> log.info("sendToKafkaByReactiveSender():  " + logSuccessResult(senderResult)))
                .doOnError(throwable -> log.error("sendToKafkaByReactiveSender(): Error of dispatch throwable = {}", throwable.getMessage()))
                .filter(senderResult -> correlationId.equals(senderResult.correlationMetadata()))
                .next();
    }

    private SenderRecord<UUID, Payment, UUID> convertToSenderRecord(Payment payment) {
        var senderRecord = SenderRecord.create(
                topic,
                0,
                System.currentTimeMillis(),
                payment.getClientId(),
                payment,
                payment.getRequestId()
        );
        senderRecord.headers().add(KafkaHeaders.CORRELATION_ID, payment.getRequestId().toString().getBytes());
        return senderRecord;
    }

    private String logSuccessResult(SenderResult<UUID> result) {
       return String.format("Successful dispatch. CorrelationId = %s, offset = %d, timestamp = %s",
               result.correlationMetadata().toString(),
               result.recordMetadata().offset(),
               new Timestamp(result.recordMetadata().timestamp()).toLocalDateTime()
               );
    }
}
