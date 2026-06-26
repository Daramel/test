package com.credit.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 加密工具类
 */
public class CryptoUtil {

    private CryptoUtil() {
    }

    /**
     * MD5加密
     */
    public static String md5(String text) {
        return SecureUtil.md5(text);
    }

    /**
     * SHA256加密
     */
    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不存在", e);
        }
    }

    /**
     * 生成雪花ID
     */
    public static long snowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成简单UUID(无横线)
     */
    public static String simpleUuid() {
        return IdUtil.simpleUUID();
    }

}
