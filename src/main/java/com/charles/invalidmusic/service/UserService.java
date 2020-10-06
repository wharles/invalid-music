package com.charles.invalidmusic.service;

import com.charles.invalidmusic.common.BaseException;
import com.charles.invalidmusic.dao.UserMapper;
import com.charles.invalidmusic.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UserService
 *
 * @author charleswang
 * @since 2020/9/27 10:14 下午
 */
@Service
public class UserService {
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) throws BaseException {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new BaseException("username already exists");
        }
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        int res = userMapper.createUser(user);
        if (res != 1) {
            throw new BaseException("register user failed");
        }
        return userMapper.findByUsername(user.getUsername());
    }
}
