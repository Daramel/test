package com.credit.controller;

import com.credit.common.ErrorCode;
import com.credit.common.Result;
import com.credit.security.JwtTokenProvider;
import com.credit.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token刷新")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        var user = sysUserService.register(
                dto.getUsername(),
                dto.getPassword(),
                dto.getRealName(),
                dto.getPhone(),
                dto.getEmail()
        );

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setToken(token);

        return Result.success("注册成功", vo);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        var user = sysUserService.findByUsername(dto.getUsername());
        if (user == null) {
            return Result.error(ErrorCode.USERNAME_PASSWORD_ERROR);
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error(ErrorCode.USERNAME_PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            return Result.error(ErrorCode.FORBIDDEN, "用户已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setToken(token);

        return Result.success("登录成功", vo);
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<String> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(ErrorCode.TOKEN_INVALID);
        }

        String oldToken = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(oldToken)) {
            return Result.error(ErrorCode.TOKEN_INVALID);
        }

        String newToken = jwtTokenProvider.refreshToken(oldToken);
        return Result.success("刷新成功", newToken);
    }

    // ========== DTO和VO ==========

    @Data
    public static class LoginDTO {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterDTO {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        @NotBlank(message = "真实姓名不能为空")
        private String realName;

        private String phone;

        private String email;
    }

    @Data
    public static class LoginVO {
        private Long userId;
        private String username;
        private String realName;
        private String token;
    }

}
