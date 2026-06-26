package com.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.credit.entity.CreditReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 征信报告Mapper
 */
@Mapper
public interface CreditReportMapper extends BaseMapper<CreditReport> {

    /**
     * 根据报告编号查询
     */
    CreditReport selectByReportNo(@Param("reportNo") String reportNo);

    /**
     * 根据归属企业ID查询报告列表
     */
    List<CreditReport> selectByOwnerEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    /**
     * 分页查询企业报告列表
     */
    Page<CreditReport> selectPageByOwnerEnterpriseId(Page<CreditReport> page, @Param("enterpriseId") Long enterpriseId);

}
