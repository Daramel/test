package com.credit.controller;

import com.credit.common.Result;
import com.credit.entity.AuthAgreement;
import com.credit.service.AuthAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 授权协议控制器
 */
@RestController
@RequestMapping("/api/agreement")
@Tag(name = "授权协议管理")
public class AuthAgreementController {

    @Autowired
    private AuthAgreementService authAgreementService;

    @GetMapping("/latest")
    @Operation(summary = "获取最新授权协议")
    public Result<AuthAgreement> getLatestAgreement() {
        AuthAgreement agreement = authAgreementService.getLatestAgreement();
        return Result.success(agreement);
    }

    @GetMapping("/{version}")
    @Operation(summary = "根据版本获取协议")
    public Result<AuthAgreement> getAgreementByVersion(@PathVariable String version) {
        AuthAgreement agreement = authAgreementService.getAgreementByVersion(version);
        return Result.success(agreement);
    }

}
