package org.example.javaspringbootjooqsample.presentation.user;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.user.UserUseCase;
import org.example.javaspringbootjooqsample.application.user.command.CreateUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.DeleteUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.FindUsersCommand;
import org.example.javaspringbootjooqsample.application.user.command.GetUserCommand;
import org.example.javaspringbootjooqsample.application.user.result.UserResult;
import org.example.javaspringbootjooqsample.application.user.result.DeleteUserResult;
import org.example.javaspringbootjooqsample.application.user.result.UpdateUserResult;
import org.example.javaspringbootjooqsample.presentation.user.request.CreateUserRequest;
import org.example.javaspringbootjooqsample.presentation.user.request.UserSearchRequest;
import org.example.javaspringbootjooqsample.presentation.user.request.DeleteUserRequest;
import org.example.javaspringbootjooqsample.presentation.user.request.UpdateUserRequest;
import org.example.javaspringbootjooqsample.presentation.user.response.UserResponse;
import org.example.javaspringbootjooqsample.presentation.user.response.DeleteUserResponse;
import org.example.javaspringbootjooqsample.presentation.user.response.UpdateUserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserUseCase userUseCase;

    @GetMapping("")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userUseCase.findAll(FindUsersCommand.empty()).stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResult user = userUseCase.findById(new GetUserCommand(id));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/detail")
    public ResponseEntity<UserResponse> getUserByUsername(@ModelAttribute UserSearchRequest request) {
        UserResult user = userUseCase.findByUsername(request.toCommand());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = request.toCommand();
        UserResult result = userUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(result));
    }

    @PutMapping("")
    public ResponseEntity<UpdateUserResponse> updateUser(@RequestBody UpdateUserRequest request) {
        UpdateUserResult result = userUseCase.update(request.toCommand());
        return ResponseEntity.ok(UpdateUserResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable Long id) {
        DeleteUserRequest request = DeleteUserRequest.from(id);
        DeleteUserResult result = userUseCase.delete(request.toCommand());
        return ResponseEntity.ok(DeleteUserResponse.from(result));
    }
}
