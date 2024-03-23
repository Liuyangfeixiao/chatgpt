package org.example.chatgpt.data.types.sdk.weixin;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.SecureUtil;

import java.util.Arrays;

public class SignatureUtil {
    /**
     * @description 验证签名
     */
    public static boolean check(String token, String signature, String timestamp, String nonce) {
        // 对 token, timestamp, nonce进行字典序排序
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);
        // 将3个字符连接到一起进行SHA1加密
        try {
            // 默认生成小写字符串
            String sh1 = SecureUtil.sha1(ArrayUtil.join(arr, ""));
            return sh1.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
