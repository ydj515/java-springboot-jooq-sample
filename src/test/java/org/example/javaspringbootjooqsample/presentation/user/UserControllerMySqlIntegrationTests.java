package org.example.javaspringbootjooqsample.presentation.user;

import io.restassured.http.ContentType;
import org.example.javaspringbootjooqsample.support.MySqlWebIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;

@Tag("integration-test")
class UserControllerMySqlIntegrationTests extends MySqlWebIntegrationTestSupport {

    @Test
    void createUserReturnsCreatedUserResponse() throws Exception {
        // given
        String requestBody = """
                {
                  "username": "new-user",
                  "password": "secret123",
                  "name": "New User",
                  "email": "new.user@example.com",
                  "userType": "USER",
                  "trialCount": 0
                }
                """;

        // when & then
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/api/users")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("username", equalTo("new-user"))
                .body("name", equalTo("New User"))
                .body("userType", equalTo("USER"));
    }

    @Test
    void deleteUserRemovesUser() throws Exception {
        // given
        // 삭제 대상 계정이 데이터셋에 준비되어 있습니다.

        // when & then
        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .delete("/api/users/2")
        .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("deletedCount", equalTo(1));

        given()
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .get("/api/users/2")
        .then()
                .statusCode(404)
                .body("code", equalTo("USER_NOT_FOUND"));
    }

    @Test
    void createUserReturnsConflictWhenUsernameAlreadyExists() throws Exception {
        // given
        String requestBody = """
                {
                  "username": "user123",
                  "password": "secret123",
                  "name": "Duplicated User",
                  "email": "dup@example.com",
                  "userType": "USER",
                  "trialCount": 0
                }
                """;

        // when & then
        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/api/users")
        .then()
                .statusCode(409)
                .body("code", equalTo("USER_ALREADY_EXISTS"));
    }

    @Test
    void addsTraceHeadersToResponse() throws Exception {
        // given
        // 요청 ID 헤더를 전달합니다.

        // when & then
        given()
                .header("X-Request-Id", "request-id-123")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader("user123"))
        .when()
                .get("/api/users/1")
        .then()
                .statusCode(200)
                .header("X-Request-Id", equalTo("request-id-123"))
                .header("X-Trace-Id", not(blankOrNullString()))
                .body("id", equalTo(1))
                .body("username", equalTo("user123"))
                .body("deletedAt", nullValue());
    }
}
