package com.credit.controller;

import com.credit.common.Result;
import com.credit.dto.SignRequest;
import com.credit.dto.SignStatusVO;
import com.credit.entity.AuthSignRecord;
import com.credit.service.AuthSignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 授权签署控制器
 */
@RestController
@RequestMapping("/api/sign")
@Tag(name = "授权签署管理")
public class AuthSignController {

    @Autowired
    private AuthSignService authSignService;

    @GetMapping("/status")
    @Operation(summary = "检查签署状态")
    public Result<SignStatusVO> checkSignStatus(@RequestParam Long enterpriseId) {
        SignStatusVO status = authSignService.checkSignStatus(enterpriseId);
        return Result.success(status);
    }

    @PostMapping
    @Operation(summary = "签署授权协议")
    public Result<Long> sign(@RequestBody SignRequest request, HttpServletRequest httpRequest) {
        if (request.getIpAddress() == null) {
            request.setIpAddress(getClientIp(httpRequest));
        }
        if (request.getDeviceInfo() == null) {
            request.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        }
        Long recordId = authSignService.createSignRecord(request);
        return Result.success(recordId);
    }

    @GetMapping("/record")
    @Operation(summary = "获取签署记录")
    public Result<AuthSignRecord> getSignRecord(@RequestParam Long enterpriseId) {
        AuthSignRecord record = authSignService.getSignRecord(enterpriseId);
        return Result.success(record);
    }

    @PostMapping("/revoke")
    @Operation(summary = "撤回授权")
    public Result<Boolean> revokeSign(@RequestParam Long enterpriseId) {
        boolean result = authSignService.revokeSign(enterpriseId);
        return Result.success(result);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
