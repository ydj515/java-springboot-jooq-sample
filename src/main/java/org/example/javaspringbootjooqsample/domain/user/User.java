package org.example.javaspringbootjooqsample.domain.user;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 179338683734956806L;

    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime lastPasswordUpdatedAt;
    private UserType userType;
    private int trialCount;
    private Set<Role> roles;


    @Builder
    public User(String username, String password, String name, String email, UserType userType) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.userType = userType;
    }

    public static User register(
            String username,
            String password,
            String name,
            String email,
            UserType userType,
            int trialCount
    ) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.name = name;
        user.email = email;
        user.userType = userType;
        user.trialCount = trialCount;
        return user;
    }

    public static User restoreForUpdate(
            Long id,
            String username,
            String password,
            String name,
            String email,
            LocalDateTime lastLoginAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            LocalDateTime lastPasswordUpdatedAt,
            UserType userType,
            int trialCount
    ) {
        User user = new User();
        user.id = id;
        user.username = username;
        user.password = password;
        user.name = name;
        user.email = email;
        user.lastLoginAt = lastLoginAt;
        user.updatedAt = updatedAt;
        user.deletedAt = deletedAt;
        user.lastPasswordUpdatedAt = lastPasswordUpdatedAt;
        user.userType = userType;
        user.trialCount = trialCount;
        return user;
    }

    public void normalizeRegistrationFields() {
        if (username != null) {
            username = username.trim();
        }

        if (name != null) {
            name = name.trim();
        }

        if (email != null) {
            email = email.trim();
        }
    }
}
