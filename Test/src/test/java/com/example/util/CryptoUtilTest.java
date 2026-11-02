package com.example.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoUtilTest {

    @Test
    void roundTripsPlainText(@TempDir Path dir) throws Exception {
        CryptoUtil crypto = CryptoUtil.loadOrCreate(dir.resolve("session.key"));

        String cipherText = crypto.encrypt("s3cret-密码");

        assertTrue(crypto.isEncrypted(cipherText));
        assertFalse(cipherText.contains("s3cret"));
        assertEquals("s3cret-密码", crypto.decrypt(cipherText));
    }

    @Test
    void sameKeyFileReusedAcrossInstances(@TempDir Path dir) throws Exception {
        Path keyFile = dir.resolve("session.key");

        String cipherText = CryptoUtil.loadOrCreate(keyFile).encrypt("hello");

        assertEquals("hello", CryptoUtil.loadOrCreate(keyFile).decrypt(cipherText));
        assertTrue(Files.size(keyFile) > 0);
    }

    @Test
    void differentKeyCannotDecrypt(@TempDir Path dir) throws Exception {
        String cipherText = CryptoUtil.loadOrCreate(dir.resolve("a.key")).encrypt("hello");
        CryptoUtil other = CryptoUtil.loadOrCreate(dir.resolve("b.key"));

        assertNull(other.decrypt(cipherText));
    }

    @Test
    void legacyPlainTextPassesThrough(@TempDir Path dir) throws Exception {
        CryptoUtil crypto = CryptoUtil.loadOrCreate(dir.resolve("session.key"));

        assertEquals("plain-password", crypto.decrypt("plain-password"));
        assertNull(crypto.decrypt(null));
    }

    @Test
    void tamperedCipherTextReturnsNull(@TempDir Path dir) throws Exception {
        CryptoUtil crypto = CryptoUtil.loadOrCreate(dir.resolve("session.key"));
        String cipherText = crypto.encrypt("hello");
        String tampered = cipherText.substring(0, cipherText.length() - 2) + "AA";

        assertNull(crypto.decrypt(tampered));
    }

    @Test
    void encryptsToDifferentCipherTextEachTime(@TempDir Path dir) throws Exception {
        CryptoUtil crypto = CryptoUtil.loadOrCreate(dir.resolve("session.key"));

        assertNotEquals(crypto.encrypt("hello"), crypto.encrypt("hello"));
        assertNull(crypto.encrypt(null));
    }

    @Test
    void nonBase64PayloadReturnsNull(@TempDir Path dir) throws Exception {
        CryptoUtil crypto = CryptoUtil.loadOrCreate(dir.resolve("session.key"));

        assertNull(crypto.decrypt("enc:v1:!!!not-base64!!!"));
    }

    @Test
    void keyFileHoldsBase64Of32Bytes(@TempDir Path dir) throws Exception {
        Path keyFile = dir.resolve("session.key");
        CryptoUtil.loadOrCreate(keyFile);

        String encoded = new String(Files.readAllBytes(keyFile), StandardCharsets.UTF_8).trim();

        assertEquals(32, java.util.Base64.getDecoder().decode(encoded).length);
    }
}
