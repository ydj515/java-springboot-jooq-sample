package org.example.javaspringbootjooqsample.presentation.user.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.user.command.GetUserByUsernameCommand;
import org.example.javaspringbootjooqsample.common.search.BaseSearchParam;

@Getter
@Setter
public class UserSearchRequest extends BaseSearchParam {
    private String username;

    public GetUserByUsernameCommand toCommand() {
        return new GetUserByUsernameCommand(username);
    }
}
