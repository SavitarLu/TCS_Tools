package com.example.demo.controller;

import com.example.demo.service.TcpProxyService;
import com.example.demo.util.SshConfig;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Controller
public class TcpProxyController {

    private static final Logger log = LoggerFactory.getLogger(TcpProxyController.class);

    private final TcpProxyService proxyService;
    private final SimpMessagingTemplate messagingTemplate;

    public TcpProxyController(TcpProxyService proxyService, SimpMessagingTemplate messagingTemplate) {
        this.proxyService = proxyService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/socket")
    public String socketPage() {
        return "socket";
    }

    /**
     * 通过 SFTP 读取远端文件内容。
     */
    @GetMapping("/api/ssh/fetch")
    @ResponseBody
    public String fetchSshFile() {
        String password = SshConfig.getPassword();
        if (password == null) {
            return "未配置 SSH 密码，请设置环境变量 TCS_SSH_PASSWORD 或提供 ssh.properties";
        }

        Session session = null;
        ChannelSftp sftp = null;
        StringBuilder fileContent = new StringBuilder();

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SshConfig.getUser(), SshConfig.getServer(), 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30_000);

            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(10_000);

            String filePath = SshConfig.getFilePath();
            try (InputStream inputStream = sftp.get(filePath);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
            }
            return fileContent.toString();
        } catch (SftpException e) {
            log.warn("SFTP 读取失败: {}", e.getMessage());
            return "SFTP操作失败: " + e.getMessage();
        } catch (JSchException e) {
            log.warn("SSH 连接失败: {}", e.getMessage());
            return "SSH连接失败: " + e.getMessage();
        } catch (IOException e) {
            log.warn("读取文件失败: {}", e.getMessage());
            return "读取文件失败: " + e.getMessage();
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @MessageMapping("/connect")
    public void connect(String connectionInfo, SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String[] parts = connectionInfo == null ? new String[0] : connectionInfo.split(":");
        if (parts.length != 2) {
            messagingTemplate.convertAndSend("/topic/messages/" + sessionId, "连接信息格式错误，应为ip:port");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            messagingTemplate.convertAndSend("/topic/messages/" + sessionId, "端口必须是数字");
            return;
        }

        proxyService.connect(sessionId, parts[0].trim(), port);
    }

    @MessageMapping("/send")
    public void sendMessage(String message, SimpMessageHeaderAccessor accessor) {
        proxyService.sendToServer(accessor.getSessionId(), message);
    }

    @MessageMapping("/disconnect")
    public void disconnect(SimpMessageHeaderAccessor accessor) {
        proxyService.disconnect(accessor.getSessionId());
    }

    /**
     * 浏览器关闭页面 / 断线时释放对应的 TCP 连接。
     */
    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        proxyService.disconnect(event.getSessionId());
    }
}
