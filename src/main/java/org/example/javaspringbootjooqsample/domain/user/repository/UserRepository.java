package org.example.javaspringbootjooqsample.domain.user.repository;

import org.example.javaspringbootjooqsample.domain.user.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    User findById(Long id);

    User findByUsername(String username);

    int save(User user);

    int update(User user);

    int deleteUserRolesByUserId(Long userId);

    int delete(Long id);
}
