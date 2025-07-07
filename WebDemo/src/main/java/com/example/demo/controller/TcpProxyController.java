package com.example.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class TcpProxyController {

    private final SimpMessagingTemplate messagingTemplate;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Socket tcpSocket;
    private BufferedReader reader; // 使用BufferedReader按行读取
    private PrintWriter writer;    // 使用PrintWriter发送消息
    private volatile boolean running = false; // 连接状态标志

    public TcpProxyController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/socket")
    public String socketPage() {
        return "socket";
    }

    @MessageMapping("/connect")
    public void connect(String connectionInfo) {
        String[] parts = connectionInfo.split(":");
        if (parts.length != 2) {
            messagingTemplate.convertAndSend("/topic/messages", "连接信息格式错误，应为ip:port");
            return;
        }

        String ip = parts[0];
        int port = Integer.parseInt(parts[1]);

        // 如果已连接，先断开
        if (running) {
            closeConnection();
        }

        try {
            tcpSocket = new Socket(ip, port);
            // 使用BufferedReader按行读取输入流
            reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            writer = new PrintWriter(tcpSocket.getOutputStream(), true);
            running = true;

            messagingTemplate.convertAndSend("/topic/messages", "已连接到服务器: " + ip + ":" + port);

            // 启动线程监听TCP服务器消息
            threadPool.submit(this::listenForServerMessages);
        } catch (IOException e) {
            messagingTemplate.convertAndSend("/topic/messages", "连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listenForServerMessages() {
        try {
            String line;
            // 使用readLine()按行读取消息，确保消息完整性
            while (running && (line = reader.readLine()) != null) {
                messagingTemplate.convertAndSend("/topic/messages", line);
            }
        } catch (IOException e) {
            if (running) {
                messagingTemplate.convertAndSend("/topic/messages", "连接断开: " + e.getMessage());
                closeConnection();
            }
        }
    }

    @MessageMapping("/send")
    public void sendMessage(String message) {
        if (!running || writer == null) {
            messagingTemplate.convertAndSend("/topic/messages", "未连接到服务器");
            return;
        }

        try {
            writer.println(message);
            messagingTemplate.convertAndSend("/topic/messages", "发送: " + message);
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/messages", "发送失败: " + e.getMessage());
            closeConnection();
        }
    }

    @MessageMapping("/disconnect")
    public void disconnect() {
        closeConnection();
        messagingTemplate.convertAndSend("/topic/messages", "已断开连接");
    }

    private void closeConnection() {
        running = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (tcpSocket != null && !tcpSocket.isClosed()) {
                tcpSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader = null;
            writer = null;
            tcpSocket = null;
        }
    }
}