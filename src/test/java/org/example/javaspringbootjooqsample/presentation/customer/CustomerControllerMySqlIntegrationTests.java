package org.example.javaspringbootjooqsample.presentation.customer;

import org.example.javaspringbootjooqsample.support.MySqlWebIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@Tag("integration-test")
class CustomerControllerMySqlIntegrationTests extends MySqlWebIntegrationTestSupport {

    @Test
    void getCustomersReturnsAllSeededCustomers() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .get("/api/customers")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3))
                .body("[0].name", equalTo("한수진"));
    }

    @Test
    void getCustomerByIdReturnsCustomer() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .get("/api/customers/1")
        .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("한수진"))
                .body("email", equalTo("sujin.han@example.com"));
    }

    @Test
    void getCustomerByMissingIdReturnsNotFound() {
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .get("/api/customers/999999")
        .then()
                .statusCode(404)
                .body("code", equalTo("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void getCustomersWithoutAuthReturnsUnauthorized() {
        given()
        .when()
                .get("/api/customers")
        .then()
                .statusCode(401);
    }
}
