package com.example.demo.controller;

import com.jcraft.jsch.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class TcpProxyController {

    private final SimpMessagingTemplate messagingTemplate;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Socket tcpSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running = false;

    // SSH服务器配置
    private static final String SSH_SERVER = "10.196.60.107";
    private static final String SSH_USER = "ls2wtcs";
    private static final String SSH_PASSWORD = "ls2wtcs";
    private static final String FILE_PATH = "/home/ls2wtcs/tcsx/cmd/tcsxList.txt";

    public TcpProxyController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/socket")
    public String socketPage() {
        return "socket";
    }

    // 使用SSH(SFTP)获取远程文件的API
    @GetMapping("/api/ssh/fetch")
    @ResponseBody
    public String fetchSshFile() {
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftp = null;
        StringBuilder fileContent = new StringBuilder();

        try {
            // 创建SSH会话
            session = jsch.getSession(SSH_USER, SSH_SERVER, 22);
            session.setPassword(SSH_PASSWORD);

            // 配置SSH会话（不验证主机密钥）
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000); // 30秒连接超时

            // 打开SFTP通道
            channel = session.openChannel("sftp");
            channel.connect(10000); // 10秒连接超时
            sftp = (ChannelSftp) channel;

            // 读取文件内容
            try (InputStream inputStream = sftp.get(FILE_PATH)) {
                if (inputStream == null) {
                    return "文件不存在: " + FILE_PATH;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
            }

            return fileContent.toString();

        } catch (JSchException e) {
            return "SSH连接失败: " + e.getMessage();
        } catch (SftpException e) {
            return "SFTP操作失败: " + e.getMessage();
        } catch (IOException e) {
            return "读取文件失败: " + e.getMessage();
        } finally {
            // 关闭连接
            try {
                if (sftp != null) sftp.disconnect();
                if (channel != null) channel.disconnect();
                if (session != null) session.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        if (running) {
            closeConnection();
        }

        try {
            tcpSocket = new Socket(ip, port);
            reader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            writer = new PrintWriter(tcpSocket.getOutputStream(), true);
            running = true;

            messagingTemplate.convertAndSend("/topic/messages", "已连接到服务器: " + ip + ":" + port);

            threadPool.submit(this::listenForServerMessages);
        } catch (IOException e) {
            messagingTemplate.convertAndSend("/topic/messages", "连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listenForServerMessages() {
        try {
            String line;
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