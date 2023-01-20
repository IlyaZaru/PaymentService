package kafkagoal.paymentservice.api;

import kafka_goal.payment_service.api.PaymentServiceApi;
import kafka_goal.payment_service.model.Payment;
import kafka_goal.payment_service.model.PaymentResponse;
import kafkagoal.paymentservice.service.KafkaSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ApiPaymentService implements PaymentServiceApi {
    private final UUID DEFAULT_REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final KafkaSenderService kafkaSenderService;

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentBySenderPost(Mono<Payment> payment, ServerWebExchange exchange) {
        return payment
                .flatMap(kafkaSenderService::sendToKafkaByReactiveSender)
                .map(this::createSuccessfulResponse)
                .onErrorResume(throwable -> {
                    log.error("Error: {}", throwable.getMessage());
                    return Mono.just(this.createFailedResponse(throwable));
                });
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentByTemplatePost(Mono<Payment> payment, ServerWebExchange exchange) {
        return payment
                .flatMap(kafkaSenderService::sendToKafkaByReactiveProducerTemplate)
                .map(this::createSuccessfulResponse)
                .onErrorResume(throwable -> {
                    log.error("Error: {}", throwable.getMessage());
                    return Mono.just(this.createFailedResponse(throwable));
                });
    }

    private ResponseEntity<PaymentResponse> createSuccessfulResponse(SenderResult<UUID> senderResult) {
        var response = new PaymentResponse()
                .status(PaymentResponse.StatusEnum.SUCCES)
                .requestId(senderResult.correlationMetadata());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private ResponseEntity<PaymentResponse> createFailedResponse(Throwable throwable) {
        var response = new PaymentResponse()
                .status(PaymentResponse.StatusEnum.ERROR)
                .requestId(DEFAULT_REQUEST_ID)
                .errorMessage(throwable.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
