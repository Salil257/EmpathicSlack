package com.empathic.proxy.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Utility class for validating Slack request signatures.
 * For production use, this should be implemented as a Spring interceptor.
 */
public class SlackSignatureValidator {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String VERSION = "v0";

    public static boolean isValid(String signingSecret, String timestamp, String body, String signature) {
        if (signingSecret == null || timestamp == null || body == null || signature == null) {
            return false;
        }

        try {
            String baseString = String.format("%s:%s:%s", VERSION, timestamp, body);
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
            String computedSignature = VERSION + "=" + bytesToHex(hash);

            return MessageDigest.isEqual(
                computedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

