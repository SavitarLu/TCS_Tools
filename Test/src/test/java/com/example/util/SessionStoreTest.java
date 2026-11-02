package com.example.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStoreTest {

    @Test
    void missingFileLoadsAsNull(@TempDir Path dir) throws Exception {
        assertNull(SessionStore.loadObject(dir.resolve("missing.dat").toFile()));
    }

    @Test
    void roundTripsSerializableObject(@TempDir Path dir) throws Exception {
        File file = dir.resolve("sessions.dat").toFile();
        List<String> sessions = new ArrayList<>();
        sessions.add("f3tst@10.192.51.11");
        sessions.add("lwts02@10.195.51.9");

        SessionStore.saveObject(file, (Serializable) sessions);

        assertEquals(sessions, SessionStore.loadObject(file));
    }

    @Test
    void downloadPathFallsBackWhenMissing(@TempDir Path dir) {
        assertEquals("/tmp", SessionStore.loadDownloadPath(dir.resolve("none").toFile(), "/tmp"));
    }

    @Test
    void downloadPathFallsBackWhenBlank(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("download_path.txt");
        Files.write(file, "   \n".getBytes("UTF-8"));

        assertEquals("/tmp", SessionStore.loadDownloadPath(file.toFile(), "/tmp"));
    }

    @Test
    void downloadPathRoundTripsAndTrims(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("nested/download_path.txt");

        SessionStore.saveDownloadPath(file.toFile(), "  /home/user/logs  ");

        assertTrue(Files.exists(file));
        assertEquals("/home/user/logs", SessionStore.loadDownloadPath(file.toFile(), "/tmp"));
    }
}
