package com.jetdrone.vertx.yoke.security;

import com.jetdrone.vertx.yoke.YokeSecurity;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Base64;

import javax.crypto.Mac;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class JWT {

    private interface Crypto {
        byte[] sign(byte[] payload);
        boolean verify(byte[] signature, byte[] payload);
    }

    private static final class CryptoMac implements Crypto {
        private final Mac mac;

        private CryptoMac(final Mac mac) {
            this.mac = mac;
        }

        @Override
        public byte[] sign(byte[] payload) {
            synchronized (mac) {
                return mac.doFinal(payload);
            }
        }

        @Override
        public boolean verify(byte[] signature, byte[] payload) {
            synchronized (mac) {
                return Arrays.equals(signature, mac.doFinal(payload));
            }
        }
    }

    private static final class CryptoSignature implements Crypto {
        private final Signature sig;

        private CryptoSignature(final Signature signature) {
            this.sig = signature;
        }

        @Override
        public byte[] sign(byte[] payload) {
            try {
                synchronized (sig) {
                    sig.update(payload);
                    return sig.sign();
                }
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean verify(byte[] signature, byte[] payload) {
            try {
                synchronized (sig) {
                    sig.update(payload);
                    return Arrays.equals(signature, sig.sign());
                }
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Map<String, Crypto> CRYPTO_MAP;

    public JWT(final YokeSecurity security) {

        Map<String, Crypto> tmp = new HashMap<>();
        try {
            tmp.put("HS256", new CryptoMac(security.getMac("HS256")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }
        try {
            tmp.put("HS384", new CryptoMac(security.getMac("HS384")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }
        try {
            tmp.put("HS512", new CryptoMac(security.getMac("HS512")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }
        try {
            tmp.put("RS256", new CryptoSignature(security.getSignature("RS256")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }

        CRYPTO_MAP = Collections.unmodifiableMap(tmp);
    }

    public JsonObject decode(final String token) {
        return decode(token, false);
    }

    public JsonObject decode(final String token, boolean noVerify) {
        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new RuntimeException("Not enough or too many segments");
        }

        // All segment should be base64
        String headerSeg = segments[0];
        String payloadSeg = segments[1];
        String signatureSeg = segments[2];

        // base64 decode and parse JSON
        JsonObject header = new JsonObject(base64urlDecode(headerSeg));
        JsonObject payload = new JsonObject(base64urlDecode(payloadSeg));

        if (!noVerify) {
            Crypto crypto = CRYPTO_MAP.get(header.getString("alg"));

            if (crypto == null) {
                throw new RuntimeException("Algorithm not supported");
            }

            // verify signature. `sign` will return base64 string.
            String signingInput = headerSeg + "." + payloadSeg;

            if (!crypto.verify(Base64.decode(base64urlUnescape(signatureSeg), Base64.DONT_BREAK_LINES), signingInput.getBytes())) {
                throw new RuntimeException("Signature verification failed");
            }
        }

        return payload;
    }

    public String encode(JsonObject payload) {
        return encode(payload, "HS256");
    }

    public String encode(JsonObject payload, String algorithm) {
        Crypto crypto = CRYPTO_MAP.get(algorithm);

        if (crypto == null) {
            throw new RuntimeException("Algorithm not supported");
        }

        // header, typ is fixed value.
        JsonObject header = new JsonObject()
                .putString("typ", "JWT")
                .putString("alg", algorithm);


        // create segments, all segment should be base64 string
        String headerSegment = base64urlEncode(header.encode());
        String payloadSegment = base64urlEncode(payload.encode());
        String signingInput = headerSegment + "." + payloadSegment;
        String signSegment = base64urlEscape(Base64.encodeBytes(crypto.sign(signingInput.getBytes()), Base64.DONT_BREAK_LINES));

        return headerSegment + "." + payloadSegment + "." + signSegment;
    }

    private static String base64urlDecode(String str) {
        return new String(Base64.decode(base64urlUnescape(str), Base64.DONT_BREAK_LINES));
    }

    private static String base64urlUnescape(String str) {
        int padding = 5 - str.length() % 4;
        StringBuilder sb = new StringBuilder(str.length() + padding);
        sb.append(str);
        for (int i = 0; i < padding; i++) {
            sb.append('=');
        }
        return sb.toString().replaceAll("\\-", "+").replaceAll("_", "/");
    }

    private static String base64urlEncode(String str) {
        return base64urlEscape(Base64.encodeBytes(str.getBytes(), Base64.DONT_BREAK_LINES));
    }

    private static String base64urlEscape(String str) {
        return str.replaceAll("\\+", "-").replaceAll("/", "_").replaceAll("=", "");
    }
}
