package com.example.chatgpt.domain.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final long JWT_TTL = 60 * 60 * 1000L * 24 * 14;  // 有效期14天，单位毫秒
    private static final String JWT_KEY = "SDFGfjjghdfHFSBJSBHJSBFNFNdKDFdaddaadaefrrSsjkdsfds121232131afasdfac";
    // 创建默认的秘钥和算法，供无参的构造方法使用
    private static final SignatureAlgorithm JWT_SignAlg = SignatureAlgorithm.HS256;

    private static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    private static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JWT_KEY);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, JWT_SignAlg.getJcaName());
    }
    public static String createJWT(String issuer, Map<String, Object> claims) {
        JwtBuilder builder = getJwtBuilder(issuer, JWT_TTL, claims);
        return builder.compact();
    }
    private static JwtBuilder getJwtBuilder(String issuer, Long ttlMillis, Map<String, Object> claims) {
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JWT_TTL;
        }

        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        String uuid = getUUID();
        return Jwts.builder()
                .setClaims(claims)
                .setId(uuid)
                .setIssuedAt(now)
                .setSubject(issuer)
                .setExpiration(expDate)
                .signWith(secretKey, JWT_SignAlg); // 需要添加签名算法，否则会识别错误
    }

    public static Claims parseJWT(String jwt) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
    // 判断jwt是否合法
    public static boolean isVerify(String jwt) {
        // 这个是官方的校验规则，这里只写了一个”校验算法“，可以自己加
        Algorithm algorithm = null;
        switch (JWT_SignAlg) {
            case HS256:
                algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(JWT_KEY));
        }
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(jwt);
        // 校验不通过会抛出异常
        // 判断合法的标准：1. 头部和荷载部分没有篡改过。2. 没有过期
        return true;
    }
}
