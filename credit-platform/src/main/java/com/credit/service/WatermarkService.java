package com.credit.service;

import java.io.InputStream;

/**
 * 水印服务接口
 */
public interface WatermarkService {

    /**
     * 添加文字水印
     *
     * @param inputStream 原始PDF输入流
     * @param text        水印文本
     * @return 添加水印后的PDF输入流
     */
    InputStream addTextWatermark(InputStream inputStream, String text);

    /**
     * 添加图片水印
     *
     * @param inputStream 原始PDF输入流
     * @param imagePath   水印图片路径
     * @param opacity     透明度(0-1)
     * @return 添加水印后的PDF输入流
     */
    InputStream addImageWatermark(InputStream inputStream, String imagePath, float opacity);

}
