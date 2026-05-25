package io.github.jxch.platform.soybean.keycloak.rective.support.rsa;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SoybeanRsaKeyHolder {

    /** keyId -> RSA（包含公私钥） */
    private final Map<String, RSA> keyMap = new ConcurrentHashMap<>();

    /** 当前最新 keyId */
    private volatile String currentKeyId;

    /** 密钥保留多久（秒）—— 太久没用就清理，避免内存泄漏 */
    private static final long KEY_TTL_SECONDS = 600;

    /** keyId -> 创建时间 */
    private final Map<String, Long> keyCreatedAt = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        rotate();
    }

    /**
     * 轮换：生成一对新密钥
     */
    public synchronized KeyPairVO rotate() {
        RSA rsa = SecureUtil.rsa();              // Hutool 一行生成 RSA
        String keyId = IdUtil.fastSimpleUUID();
        keyMap.put(keyId, rsa);
        keyCreatedAt.put(keyId, System.currentTimeMillis());
        currentKeyId = keyId;

        cleanExpired();
        log.info("生成新 RSA 密钥对，keyId={}", keyId);

        return new KeyPairVO(keyId, rsa.getPublicKeyBase64());
    }

    /**
     * 给前端：返回 keyId + 公钥
     */
    public KeyPairVO currentPublicKey() {
        RSA rsa = keyMap.get(currentKeyId);
        return new KeyPairVO(currentKeyId, rsa.getPublicKeyBase64());
    }

    /**
     * 解密：根据 keyId 找私钥
     */
    public String decrypt(String keyId, String encryptedBase64) {
        RSA rsa = keyMap.get(keyId);
        if (rsa == null) {
            throw new IllegalArgumentException("公钥已失效，请重新获取");
        }
        return rsa.decryptStr(encryptedBase64, KeyType.PrivateKey);
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        keyCreatedAt.entrySet().removeIf(e -> {
            boolean expired = (now - e.getValue()) > KEY_TTL_SECONDS * 1000
                    && !e.getKey().equals(currentKeyId);
            if (expired) keyMap.remove(e.getKey());
            return expired;
        });
    }

    @Data
    @AllArgsConstructor
    public static class KeyPairVO {
        private String keyId;
        private String publicKey;   // Base64
    }
}
