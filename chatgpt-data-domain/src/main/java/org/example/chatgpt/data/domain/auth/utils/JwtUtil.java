package org.example.chatgpt.data.domain.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {
    private static final long JWT_TTL = 60 * 60 * 1000L * 24 * 7;  // 有效期7天，单位毫秒
    private static final String JWT_Secret = "SDFGfjjghdfHFSBJSBHJSBFNFNdKDFdaddaadaefrrSsjkdsfds";
    private static final SecretKey JWT_KEY = Keys.hmacShaKeyFor(JWT_Secret.getBytes());
    // 创建默认的秘钥和算法，供无参的构造方法使用
    private static final SecureDigestAlgorithm<SecretKey, SecretKey> JWT_SignAlg = Jwts.SIG.HS256;
    
    private static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    public static String encode(String issuer, long ttlMills, Map<String, Object> claims) {
        JwtBuilder builder = getJwtBuilder(issuer, ttlMills, claims);
        return builder.compact();
    }
    public static Claims decode(String jwt) {
        return Jwts.parser()
                .verifyWith(JWT_KEY)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
    public static boolean isVerify(String jwt) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(JWT_Secret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(jwt);
            // 校验不通过会抛出异常
            // 判断合法的标准：1. 头部和荷载部分没有篡改过。2. 没有过期
            return true;
        } catch (Exception e) {
            log.error("JWT 验证失败", e);
            return false;
        }
        
    }
    private static JwtBuilder getJwtBuilder(String issuer, Long ttlMillis, Map<String, Object> claims) {
        long nowMills = System.currentTimeMillis();
        Date now = new Date(nowMills);
        if (ttlMillis == null) {
            ttlMillis = JWT_TTL;
        }
        long expMills = nowMills + ttlMillis;
        Date expDate = new Date(expMills);
        String uuid = getUUID();
        return Jwts.builder()
                .claims(claims)
                .id(uuid)
                .subject(issuer)
                .issuedAt(now)
                .expiration(expDate)
                .signWith(JWT_KEY, JWT_SignAlg);
    }
}
