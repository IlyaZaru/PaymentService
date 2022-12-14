package kafkagoal.paymentservice.api;

import kafka_goal.payment_service.api.PaymentServiceApi;
import kafka_goal.payment_service.model.Payment;
import kafka_goal.payment_service.model.PaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class PaymentController implements PaymentServiceApi {
    private final UUID DEFAULT_REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public Mono<ResponseEntity<PaymentResponse>> paymentPost(Mono<Payment> payment, ServerWebExchange exchange) {
        return payment
                .map(requestPayment -> {
                    var response = new PaymentResponse()
                            .status(PaymentResponse.StatusEnum.SUCCES)
                            .requestId(requestPayment.getRequestId());
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                })
                .onErrorResume(throwable -> {
                    var response = new PaymentResponse()
                            .status(PaymentResponse.StatusEnum.ERROR)
                            .requestId(DEFAULT_REQUEST_ID)
                            .errorMessage(throwable.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
                });
    }
}
