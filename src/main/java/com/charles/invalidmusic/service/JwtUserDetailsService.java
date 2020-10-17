package com.charles.invalidmusic.service;

import com.charles.invalidmusic.dao.UserMapper;
import com.charles.invalidmusic.model.JwtUser;
import com.charles.invalidmusic.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * JwtUserDetailsService
 *
 * @author charleswang
 * @since 2020/9/27 8:52 下午
 */
@Service("UserDetailsService")
public class JwtUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Autowired
    public JwtUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        var user = userMapper.findByUsername(s);
        if (user == null) {
            throw new UsernameNotFoundException("can not find username " + s);
        }
        return new JwtUser(user);
    }
}
