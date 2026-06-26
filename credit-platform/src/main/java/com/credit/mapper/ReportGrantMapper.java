package com.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.credit.entity.ReportGrant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报告授权Mapper
 */
@Mapper
public interface ReportGrantMapper extends BaseMapper<ReportGrant> {

    /**
     * 根据被授权方企业ID查询有效授权
     */
    List<ReportGrant> selectValidByGranteeEnterpriseId(@Param("granteeEnterpriseId") Long granteeEnterpriseId);

    /**
     * 根据授权编号查询
     */
    ReportGrant selectByGrantNo(@Param("grantNo") String grantNo);

}
