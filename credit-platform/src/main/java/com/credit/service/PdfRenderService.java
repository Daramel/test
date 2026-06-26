package com.credit.service;

import java.io.InputStream;
import java.util.Map;

/**
 * PDF渲染服务接口
 */
public interface PdfRenderService {

    /**
     * 渲染HTML模板为PDF
     *
     * @param templatePath 模板路径
     * @param data         模板数据
     * @return PDF输入流
     */
    InputStream renderToPdf(String templatePath, Map<String, Object> data);

    /**
     * 渲染HTML内容为PDF
     *
     * @param htmlContent HTML内容
     * @return PDF输入流
     */
    InputStream renderHtmlToPdf(String htmlContent);

}
