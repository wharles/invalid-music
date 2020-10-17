package com.charles.invalidmusic.core.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * EncryptionUtil
 *
 * @author charleswang
 * @since 2020/8/30 12:59 下午
 */
public final class EncryptionUtil {

    private EncryptionUtil() {
    }

    /**
     * AES加密
     * 此处使用AES-128-CBC加密模式，key需要为16位
     *
     * @param sSrc 加密内容
     * @param sKey 偏移量
     * @param sIV  sIV
     * @return 密文
     */
    public static String encrypt(String sSrc, String sKey, String sIV) throws GeneralSecurityException {

        byte[] raw = sKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(sIV.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());

        return new String(Base64.encodeBase64(encrypted));
    }

    /**
     * 生产指定位数16进制随机数
     *
     * @param len 长度
     * @return 16进制随机数
     */
    public static String getRandomHex(int len) {
        var result = new StringBuilder();
        for (var i = 0; i < len; i++) {
            result.append(Integer.toHexString(new Random().nextInt(16)));
        }
        return result.toString().toUpperCase();
    }

    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
