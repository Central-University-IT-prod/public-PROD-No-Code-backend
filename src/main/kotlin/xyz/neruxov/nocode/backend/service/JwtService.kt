package xyz.neruxov.nocode.backend.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey


/**
 * @author <a href="https://github.com/ali-bouali/spring-boot-3-jwt-security/blob/main/src/main/java/com/alibou/security/config/JwtService.java">ali-bouali</a>
 */
@Service
class JwtService {

    @Value("\${spring.security.jwt.secret}")
    private val secretKey: String = "secret"

    @Value("\${spring.security.jwt.expiration}")
    private val jwtExpiration: Long = 24 * 60 * 60

    @Value("\${spring.security.jwt.refresh.expiration}")
    private val jwtRefreshExpiration: Long = 30 * 24 * 60 * 60

    fun extractUsername(token: String?): String? {
        return extractClaim<String>(token, Claims::getSubject)
    }

    fun <T> extractClaim(token: String?, claimsResolver: (Claims) -> T): T? {
        val claims: Claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(HashMap(), userDetails, jwtExpiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return generateToken(mapOf("refresh" to true), userDetails, jwtRefreshExpiration)
    }

    fun generateToken(
        extraClaims: Map<String?, Any?>,
        userDetails: UserDetails,
        expiration: Long
    ): String {
        val nonce = UUID.randomUUID().toString()
        val updatedClaims = HashMap(extraClaims)
        updatedClaims["nonce"] = nonce

        return buildToken(updatedClaims, userDetails, expiration)
    }

    private fun buildToken(
        extraClaims: Map<String?, Any?>,
        userDetails: UserDetails,
        expiration: Long
    ): String {
        return Jwts
            .builder()
            .claims(extraClaims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(getSignInKey(), Jwts.SIG.HS256)
            .compact()
    }

    fun isTokenValid(token: String?, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String?): Boolean {
        return extractExpiration(token)?.before(Date()) ?: true
    }

    private fun extractExpiration(token: String?): Date? {
        return extractClaim<Date>(token, Claims::getExpiration)
    }

    private fun extractAllClaims(token: String?): Claims {
        return Jwts
            .parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getSignInKey(): SecretKey {
        val keyBytes: ByteArray = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

}