package com.credit.service.impl;

import com.credit.service.PdfRenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * PDF渲染服务实现
 */
@Slf4j
@Service
public class PdfRenderServiceImpl implements PdfRenderService {

    private final TemplateEngine templateEngine;

    public PdfRenderServiceImpl() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/report/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        this.templateEngine = new SpringTemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    @Override
    public InputStream renderToPdf(String templatePath, Map<String, Object> data) {
        try {
            // 1. 使用Thymeleaf渲染HTML
            Context context = new Context();
            context.setVariables(data);

            StringWriter writer = new StringWriter();
            templateEngine.process(templatePath, context, writer);
            String htmlContent = writer.toString();

            // 2. 转换为PDF（简化版本，实际应使用Flying Saucer + iText）
            return renderHtmlToPdf(htmlContent);
        } catch (Exception e) {
            log.error("渲染模板失败: templatePath={}", templatePath, e);
            throw new RuntimeException("渲染模板失败", e);
        }
    }

    @Override
    public InputStream renderHtmlToPdf(String htmlContent) {
        try {
            // 简化实现：直接返回HTML内容的字节数组
            // 实际生产环境应使用Flying Saucer + iText将HTML转换为PDF
            // 例如:
            // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // ITextRenderer renderer = new ITextRenderer();
            // renderer.setDocumentFromString(htmlContent);
            // renderer.layout();
            // renderer.createPDF(outputStream);
            // return new ByteArrayInputStream(outputStream.toByteArray());

            log.info("HTML转PDF处理（简化模式）");

            // 返回HTML作为字节流（测试用）
            // 实际使用时应调用PDF转换库
            return new ByteArrayInputStream(
                    htmlContent.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("HTML转PDF失败", e);
            throw new RuntimeException("HTML转PDF失败", e);
        }
    }

}
