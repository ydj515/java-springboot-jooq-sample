package org.example.javaspringbootjooqsample.application.customer.command;

public record FindCustomersCommand() {
    public static FindCustomersCommand empty() {
        return new FindCustomersCommand();
    }
}
