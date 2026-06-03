package org.example.javaspringbootjooqsample.presentation.user.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.user.command.UpdateUserCommand;
import org.example.javaspringbootjooqsample.domain.user.UserType;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateUserRequest {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private LocalDateTime lastLoginAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime lastPasswordUpdatedAt;
    private UserType userType;
    private int trialCount;

    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(
                id,
                username,
                password,
                name,
                email,
                lastLoginAt,
                updatedAt,
                deletedAt,
                lastPasswordUpdatedAt,
                userType,
                trialCount
        );
    }
}
