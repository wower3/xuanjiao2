package com.xuanjiao.app.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 *
 * <p>提供JWT令牌的生成、解析和验证功能。使用HS512算法进行签名。</p>
 *
 * <p>配置项：</p>
 * <ul>
 *   <li>jwt.secret - 签名密钥</li>
 *   <li>jwt.expiration - 令牌过期时间（毫秒）</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Component
public class JwtUtil {

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT令牌过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     *
     * <p>根据用户ID和用户名生成JWT令牌，包含用户ID作为自定义声明。</p>
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * <p>解析JWT令牌并返回声明信息。如果令牌无效或已过期，将抛出异常。</p>
     *
     * @param token JWT令牌字符串
     * @return 令牌中的声明信息
     * @throws io.jsonwebtoken.ExpiredJwtException 令牌已过期
     * @throws io.jsonwebtoken.SignatureException 签名验证失败
     * @throws io.jsonwebtoken.MalformedJwtException 令牌格式错误
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌字符串
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 检查令牌是否已过期
     *
     * @param token JWT令牌字符串
     * @return 如果令牌已过期返回true，否则返回false
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }
}
