package com.charles.invalidmusic.core.netease.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * EncryptionUtil
 *
 * @author charleswang
 * @since 2020/8/30 12:59 下午
 */
public final class EncryptionUtil {

    private EncryptionUtil() {
    }

    public static final String SECRET_KEY = "TA3YiYCfY2dDJQgg";
    public static final String ENC_SEC_KEY = "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210";
    public static final String NONCE = "0CoJUm6Qyw8W8jud";
    public static final String IV = "0102030405060708";

    /**
     * AES加密
     * 此处使用AES-128-CBC加密模式，key需要为16位
     *
     * @param sSrc 加密内容
     * @param sKey 偏移量
     * @return 密文
     */
    public static String encrypt(String sSrc, String sKey) throws GeneralSecurityException {

        byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());

        return new String(Base64.encodeBase64(encrypted));
    }
}
