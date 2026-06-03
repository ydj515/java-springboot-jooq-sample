package org.example.javaspringbootjooqsample.presentation.order;

import io.restassured.http.ContentType;
import org.example.javaspringbootjooqsample.support.MySqlWebIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("integration-test")
class OrderControllerMySqlIntegrationTests extends MySqlWebIntegrationTestSupport {

    @Test
    void payEndpointTransitionsCreatedOrderToPaid() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .header("Idempotency-Key", "test-pay-1-" + System.nanoTime())
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/1/pay")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("status", equalTo("PAID"))
                .body("version", equalTo(1))
                .body("paidAt", notNullValue())
                .body("paymentKey", notNullValue());
    }

    @Test
    void payEndpointReturns400WhenIdempotencyKeyMissing() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/1/pay")
        .then()
                .statusCode(400)
                .body("code", equalTo("IDEMPOTENCY_KEY_REQUIRED"));
    }

    @Test
    void payEndpointReplaysExistingPaymentForSameIdempotencyKey() {
        String key = "replay-test-" + System.nanoTime();

        // 첫 결제: PG 호출, 새 paymentKey 발급
        String firstPaymentKey = given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .header("Idempotency-Key", key)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/1/pay")
        .then()
                .statusCode(200)
                .extract().path("paymentKey");

        // 같은 키 재요청: PG 재호출 없이 동일 paymentKey replay
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .header("Idempotency-Key", key)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/1/pay")
        .then()
                .statusCode(200)
                .body("paymentKey", equalTo(firstPaymentKey));
    }

    @Test
    void payEndpointReturns409WhenIdempotencyKeyReusedForDifferentOrder() {
        String key = "conflict-test-" + System.nanoTime();

        // order 1로 첫 결제
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .header("Idempotency-Key", key)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/1/pay")
        .then()
                .statusCode(200);

        // 같은 키 + 다른 order → 409 IDEMPOTENCY_KEY_CONFLICT
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .header("Idempotency-Key", key)
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/2/pay")
        .then()
                .statusCode(409)
                .body("code", equalTo("IDEMPOTENCY_KEY_CONFLICT"));
    }

    @Test
    void shipEndpointTransitionsPaidOrderToShipped() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/2/ship")
        .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("status", equalTo("SHIPPED"))
                .body("version", equalTo(2))
                .body("shippedAt", notNullValue());
    }

    @Test
    void cancelEndpointRejectsShippedOrder() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
                .header("Idempotency-Key", "test-cancel-rejected-" + System.nanoTime())
                .contentType(ContentType.JSON)
        .when()
                .post("/api/orders/3/cancel")
        .then()
                .statusCode(409)
                .body("code", equalTo("INVALID_ORDER_STATUS_TRANSITION"));
    }

    @Test
    void statusProjectionEndpointReturnsSummaries() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .get("/api/orders/status/PAID/summaries")
        .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].status", equalTo("PAID"))
                .body("[0].createdAt", notNullValue());
    }
}
