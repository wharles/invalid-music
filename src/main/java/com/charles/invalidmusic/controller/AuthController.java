package com.charles.invalidmusic.controller;

import com.charles.invalidmusic.common.BaseException;
import com.charles.invalidmusic.common.Response;
import com.charles.invalidmusic.config.JwtTokenUtil;
import com.charles.invalidmusic.model.JwtUser;
import com.charles.invalidmusic.model.User;
import com.charles.invalidmusic.model.UserDto;
import com.charles.invalidmusic.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * UserController
 *
 * @author charleswang
 * @since 2020/9/26 12:55 下午
 */
@Api("用户授权管理接口")
@RequestMapping("/api/auth")
@Controller
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @ApiOperation("注册接口")
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Response<User> register(@RequestBody @ApiParam(name = "user", value = "注册用户对象json", required = true) User user) throws BaseException {
        User resultUser = userService.registerUser(user);
        return new Response<>(resultUser);
    }

    @ApiOperation("登入接口")
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Response<Map.Entry<String, String>> login(@RequestBody @ApiParam(name = "userDto", value = "用户对象json", required = true) UserDto userDto) throws BaseException {
        Authentication authentication = authenticate(userDto.getUsername(), userDto.getPassword());
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        LOGGER.info("jwtUser: {}", jwtUser.toString());

        String role = "";
        Collection<? extends GrantedAuthority> authorities = jwtUser.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            role = authority.getAuthority();
        }

        String token = JwtTokenUtil.createToken(jwtUser.getUsername(), role);
        return new Response<>(new AbstractMap.SimpleEntry<>("token", JwtTokenUtil.TOKEN_PREFIX + token));
    }

    private Authentication authenticate(String username, String password) throws BaseException {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>()));
        } catch (DisabledException | BadCredentialsException e) {
            throw new BaseException(e.getMessage());
        }
    }
}
