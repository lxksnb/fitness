package com.fitness.service;

import com.fitness.common.ResultCode;
import com.fitness.dto.LoginDTO;
import com.fitness.dto.RegisterDTO;
import com.fitness.dto.TokenResponse;
import com.fitness.entity.SysUser;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.SysUserMapper;
import com.fitness.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final long accessExpiration;

    public AuthService(SysUserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       @Value("${jwt.access-token-expiration}") long accessExpiration) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.accessExpiration = accessExpiration;
    }

    public TokenResponse login(LoginDTO dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账户已被禁用");
        }
        return buildTokenResponse(user);
    }

    @Transactional
    public void register(RegisterDTO dto) {
        if (userMapper.countByUsername(dto.getUsername()) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);
    }

    public TokenResponse refresh(String refreshToken) {
        if (!jwtUtils.validate(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "refreshToken无效或已过期");
        }
        Long userId = jwtUtils.getUserId(refreshToken);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return buildTokenResponse(user);
    }

    private TokenResponse buildTokenResponse(SysUser user) {
        TokenResponse resp = new TokenResponse();
        resp.setAccessToken(jwtUtils.createAccessToken(user.getId(), user.getUsername()));
        resp.setRefreshToken(jwtUtils.createRefreshToken(user.getId()));
        resp.setExpiresIn(accessExpiration / 1000);
        resp.setUserId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        return resp;
    }
}
