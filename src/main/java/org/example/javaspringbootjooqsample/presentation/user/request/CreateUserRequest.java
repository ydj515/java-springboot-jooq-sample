package org.example.javaspringbootjooqsample.presentation.user.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.user.command.CreateUserCommand;
import org.example.javaspringbootjooqsample.domain.user.UserType;

@Getter
@Setter
public class CreateUserRequest {
    private String username;
    private String password;
    private String name;
    private String email;
    private UserType userType;
    private int trialCount;

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(
                username,
                password,
                name,
                email,
                userType,
                trialCount
        );
    }
}
