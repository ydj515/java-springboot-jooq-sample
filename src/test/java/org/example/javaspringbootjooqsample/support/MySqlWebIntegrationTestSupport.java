package org.example.javaspringbootjooqsample.support;

import io.restassured.RestAssured;
import org.example.javaspringbootjooqsample.infrastructure.security.AuthConstants;
import org.example.javaspringbootjooqsample.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class MySqlWebIntegrationTestSupport extends MySqlIntegrationTestSupport {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected String authorizationHeader(String username) {
        JwtTokenProvider.IssuedAccessToken token = jwtTokenProvider.issueAccessToken(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        return token.tokenType() + AuthConstants.BEARER_SEPARATOR + token.accessToken();
    }
}
