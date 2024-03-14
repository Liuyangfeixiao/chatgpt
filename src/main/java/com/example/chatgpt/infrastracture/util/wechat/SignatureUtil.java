package com.example.chatgpt.infrastracture.util.wechat;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.SecureUtil;

import java.util.Arrays;

public class SignatureUtil {
    public static boolean check(String signature, String timestamp, String nonce, String token) {
        // 对token, nonce, timestamp进行字典序排序
        String[] arr = new String[]{timestamp, nonce, token};
        Arrays.sort(arr);
        // 将三个字符串拼接在一起进行SHA1加密
        try {
            // 默认生成小写字符串
            String sh1 = SecureUtil.sha1(ArrayUtil.join(arr, ""));
            return signature.equals(sh1);
        } catch (Exception e) {
            return false;
        }
    }
}
