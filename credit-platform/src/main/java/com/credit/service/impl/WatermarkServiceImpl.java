package com.credit.service.impl;

import com.credit.service.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 水印服务实现
 */
@Slf4j
@Service
public class WatermarkServiceImpl implements WatermarkService {

    @Override
    public InputStream addTextWatermark(InputStream inputStream, String text) {
        try {
            // 读取原始PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] pdfBytes = outputStream.toByteArray();

            // 由于完整的PDF水印功能需要iText库，这里返回原始PDF
            // 实际生产环境应使用PDF库添加水印
            log.info("添加文字水印: {}", text);

            return new ByteArrayInputStream(pdfBytes);
        } catch (Exception e) {
            log.error("添加水印失败", e);
            throw new RuntimeException("添加水印失败", e);
        }
    }

    @Override
    public InputStream addImageWatermark(InputStream inputStream, String imagePath, float opacity) {
        try {
            // 读取原始PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] pdfBytes = outputStream.toByteArray();

            // 由于完整的PDF水印功能需要iText库，这里返回原始PDF
            // 实际生产环境应使用PDF库添加图片水印
            log.info("添加图片水印: {}, opacity: {}", imagePath, opacity);

            return new ByteArrayInputStream(pdfBytes);
        } catch (Exception e) {
            log.error("添加图片水印失败", e);
            throw new RuntimeException("添加图片水印失败", e);
        }
    }

}
