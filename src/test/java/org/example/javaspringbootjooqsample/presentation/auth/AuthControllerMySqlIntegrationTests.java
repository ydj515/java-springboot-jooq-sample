package org.example.javaspringbootjooqsample.presentation.auth;

import io.restassured.http.ContentType;
import org.example.javaspringbootjooqsample.support.MySqlWebIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

@Tag("integration-test")
class AuthControllerMySqlIntegrationTests extends MySqlWebIntegrationTestSupport {

    @Test
    void loginReturnsAccessTokenAndAuthorizationHeader() {
        // given
        String requestBody = """
                {
                  "username": "user123",
                  "password": "password123"
                }
                """;

        // when & then
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/api/auth/login")
        .then()
                .statusCode(200)
                .header(HttpHeaders.AUTHORIZATION, not(blankOrNullString()))
                .body("username", equalTo("user123"))
                .body("tokenType", equalTo("Bearer"))
                .body("accessToken", not(blankOrNullString()));
    }
}
