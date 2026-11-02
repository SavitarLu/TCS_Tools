package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * TCP 代理服务。
 *
 * <p>每个浏览器会话（STOMP session）各自持有一条 TCP 连接，互不影响；
 * 服务端消息只推送给发起连接的那个会话。</p>
 */
@Service
public class TcpProxyService {

    private static final Logger log = LoggerFactory.getLogger(TcpProxyService.class);
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final ExecutorService listenerPool = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "tcp-listener");
        thread.setDaemon(true);
        return thread;
    });

    public TcpProxyService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 为指定会话建立 TCP 连接，已存在旧连接时先关闭。
     */
    public void connect(String sessionId, String ip, int port) {
        close(sessionId);
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS);
            Connection connection = new Connection(sessionId, socket);
            connections.put(sessionId, connection);
            send(sessionId, "已连接到服务器: " + ip + ":" + port);
            connection.listener = listenerPool.submit(() -> listen(connection));
        } catch (IOException e) {
            log.warn("连接 {}:{} 失败: {}", ip, port, e.getMessage());
            send(sessionId, "连接失败: " + e.getMessage());
        }
    }

    public void sendToServer(String sessionId, String message) {
        Connection connection = connections.get(sessionId);
        if (connection == null || !connection.running) {
            send(sessionId, "未连接到服务器");
            return;
        }
        try {
            connection.writer.println(message);
        } catch (Exception e) {
            send(sessionId, "发送失败: " + e.getMessage());
            close(connection);
        }
    }

    public void disconnect(String sessionId) {
        close(sessionId);
        send(sessionId, "已断开连接");
    }

    private void listen(Connection connection) {
        try {
            String line;
            while (connection.running && (line = connection.reader.readLine()) != null) {
                send(connection.sessionId, line);
            }
        } catch (IOException e) {
            if (connection.running) {
                send(connection.sessionId, "连接断开: " + e.getMessage());
            }
        } finally {
            close(connection);
        }
    }

    private void send(String sessionId, String payload) {
        messagingTemplate.convertAndSend("/topic/messages/" + sessionId, payload);
    }

    private void close(String sessionId) {
        Connection connection = connections.remove(sessionId);
        if (connection != null) {
            close(connection);
        }
    }

    private void close(Connection connection) {
        connection.running = false;
        connections.remove(connection.sessionId, connection);
        Future<?> listener = connection.listener;
        if (listener != null && !listener.isDone()) {
            listener.cancel(true);
        }
        try {
            connection.socket.close();
        } catch (IOException e) {
            log.debug("关闭 socket 失败: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        connections.keySet().forEach(this::close);
        listenerPool.shutdownNow();
    }

    /**
     * 单条 TCP 连接及其读写资源。
     */
    private static final class Connection {
        private final String sessionId;
        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private volatile boolean running = true;
        private volatile Future<?> listener;

        private Connection(String sessionId, Socket socket) throws IOException {
            this.sessionId = sessionId;
            this.socket = socket;
            this.reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }
    }
}
