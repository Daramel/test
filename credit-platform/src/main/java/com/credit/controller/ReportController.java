package com.credit.controller;

import com.credit.common.ErrorCode;
import com.credit.common.PageResult;
import com.credit.common.Result;
import com.credit.dto.ReportCreateRequest;
import com.credit.dto.ReportDetailVO;
import com.credit.dto.ReportListVO;
import com.credit.dto.ReportProgressVO;
import com.credit.entity.CreditReport;
import com.credit.entity.ReportGrant;
import com.credit.entity.SysUser;
import com.credit.mapper.CreditReportMapper;
import com.credit.mapper.ReportGrantMapper;
import com.credit.mapper.SysUserMapper;
import com.credit.security.UserPrincipal;
import com.credit.service.ReportGeneratorService;
import com.credit.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

/**
 * 报告控制器
 */
@Tag(name = "报告管理", description = "报告创建、查询、下载等")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportGeneratorService reportGeneratorService;
    private final CreditReportMapper creditReportMapper;
    private final ReportGrantMapper reportGrantMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 创建报告（触发生成）
     */
    @Operation(summary = "创建报告", description = "创建报告并触发生成")
    @PostMapping("/create")
    public Result<Long> createReport(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Long reportId = reportService.createReport(
                request.getEnterpriseId(),
                user.getUserId(),
                request.getReportType(),
                request.getOrderId()
        );
        return Result.success("报告创建成功", reportId);
    }

    /**
     * 获取报告详情
     */
    @Operation(summary = "获取报告详情")
    @GetMapping("/{id}")
    public Result<ReportDetailVO> getReportDetail(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "报告ID") @PathVariable("id") Long reportId
    ) {
        // 检查权限
        if (!reportService.checkAccessPermission(user.getUserId(), reportId)) {
            return Result.error(ErrorCode.FORBIDDEN, "无权限访问此报告");
        }

        ReportDetailVO detail = reportService.getReportDetail(reportId);
        return Result.success(detail);
    }

    /**
     * 预览报告
     */
    @Operation(summary = "预览报告")
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewReport(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "报告ID") @PathVariable("id") Long reportId
    ) {
        // 检查权限
        if (!reportService.checkAccessPermission(user.getUserId(), reportId)) {
            return ResponseEntity.status(403).build();
        }

        CreditReport report = creditReportMapper.selectById(reportId);
        if (report == null || report.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(report.getFileUrl());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    /**
     * 下载报告
     */
    @Operation(summary = "下载报告")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadReport(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "报告ID") @PathVariable("id") Long reportId
    ) {
        // 检查权限
        if (!reportService.checkAccessPermission(user.getUserId(), reportId)) {
            return ResponseEntity.status(403).build();
        }

        CreditReport report = creditReportMapper.selectById(reportId);
        if (report == null || report.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(report.getFileUrl());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.getReportNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    /**
     * 获取生成进度
     */
    @Operation(summary = "获取报告生成进度")
    @GetMapping("/{id}/progress")
    public Result<ReportProgressVO> getReportProgress(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "报告ID") @PathVariable("id") Long reportId
    ) {
        CreditReport report = creditReportMapper.selectById(reportId);
        if (report == null) {
            return Result.error(ErrorCode.PARAM_ERROR, "报告不存在");
        }

        ReportProgressVO vo = new ReportProgressVO();
        vo.setReportId(reportId);
        vo.setReportNo(report.getReportNo());
        vo.setStatus(report.getStatus());
        vo.setProgress(reportGeneratorService.getProgress(reportId));
        vo.setCurrentStep(reportGeneratorService.getCurrentStep(reportId));

        return Result.success(vo);
    }

    /**
     * 获取报告列表
     */
    @Operation(summary = "获取报告列表")
    @GetMapping("/list")
    public Result<PageResult<ReportListVO>> getReportList(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        SysUser sysUser = sysUserMapper.selectById(user.getUserId());
        Long enterpriseId = sysUser.getEnterpriseId();

        PageResult<ReportListVO> result = reportService.getReportList(enterpriseId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取被授权报告列表
     */
    @Operation(summary = "获取被授权报告列表")
    @GetMapping("/granted-list")
    public Result<List<ReportGrantVO>> getGrantedReportList(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        SysUser sysUser = sysUserMapper.selectById(user.getUserId());
        List<ReportGrant> grants = reportGrantMapper.selectValidByGranteeEnterpriseId(sysUser.getEnterpriseId());

        List<ReportGrantVO> voList = grants.stream().map(grant -> {
            ReportGrantVO vo = new ReportGrantVO();
            vo.setId(grant.getId());
            vo.setGrantNo(grant.getGrantNo());
            vo.setGrantEnterpriseId(grant.getGrantEnterpriseId());
            vo.setGranteeEnterpriseId(grant.getGranteeEnterpriseId());
            vo.setGrantScope(grant.getGrantScope());
            vo.setGrantTime(grant.getGrantTime());
            vo.setExpireTime(grant.getExpireTime());
            vo.setStatus(grant.getStatus());
            return vo;
        }).toList();

        return Result.success(voList);
    }

    /**
     * 查看被授权报告
     */
    @Operation(summary = "查看被授权报告")
    @GetMapping("/granted/{grantId}")
    public Result<ReportDetailVO> getGrantedReport(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "授权ID") @PathVariable("grantId") Long grantId
    ) {
        ReportGrant grant = reportGrantMapper.selectById(grantId);
        if (grant == null) {
            return Result.error(ErrorCode.PARAM_ERROR, "授权记录不存在");
        }

        SysUser sysUser = sysUserMapper.selectById(user.getUserId());
        if (!grant.getGranteeEnterpriseId().equals(sysUser.getEnterpriseId())) {
            return Result.error(ErrorCode.FORBIDDEN, "无权限访问此授权报告");
        }

        if (grant.getStatus() != 1) {
            return Result.error(ErrorCode.PARAM_ERROR, "授权已过期或被撤销");
        }

        // 返回授权报告列表中的第一个报告详情
        // 实际业务中可能需要更复杂的逻辑
        return Result.success(null);
    }

    // ========== DTO ==========

    @Data
    public static class ReportGrantVO {
        private Long id;
        private String grantNo;
        private Long grantEnterpriseId;
        private Long granteeEnterpriseId;
        private Integer grantScope;
        private java.time.LocalDateTime grantTime;
        private java.time.LocalDateTime expireTime;
        private Integer status;
    }

}
