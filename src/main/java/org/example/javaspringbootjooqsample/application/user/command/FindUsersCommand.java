package org.example.javaspringbootjooqsample.application.user.command;

public record FindUsersCommand() {
    public static FindUsersCommand empty() {
        return new FindUsersCommand();
    }
}
