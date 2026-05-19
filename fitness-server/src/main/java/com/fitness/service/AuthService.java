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

/**
 * 认证服务
 * 处理用户登录、注册和令牌刷新等认证相关业务逻辑
 */
@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    /** 访问令牌过期时间（毫秒） */
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

    /**
     * 用户登录
     * 验证用户名和密码，返回包含 JWT 令牌的响应
     *
     * @param dto 登录请求参数
     * @return 包含访问令牌和刷新令牌的响应
     * @throws BusinessException 用户名或密码错误时抛出
     */
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

    /**
     * 用户注册
     * 创建新用户账号，密码使用 BCrypt 加密，默认角色为 USER
     *
     * @param dto 注册请求参数
     * @throws BusinessException 用户名已存在时抛出
     */
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

    /**
     * 刷新令牌
     * 使用 refreshToken 获取新的访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的令牌响应
     * @throws BusinessException 令牌无效或已过期时抛出
     */
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

    /**
     * 构建令牌响应
     * 根据用户信息生成包含访问令牌、刷新令牌和用户信息的响应对象
     */
    private TokenResponse buildTokenResponse(SysUser user) {
        TokenResponse resp = new TokenResponse();
        resp.setAccessToken(jwtUtils.createAccessToken(user.getId(), user.getUsername()));
        resp.setRefreshToken(jwtUtils.createRefreshToken(user.getId()));
        resp.setExpiresIn(accessExpiration / 1000); // 转换为秒
        resp.setUserId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        return resp;
    }
}
