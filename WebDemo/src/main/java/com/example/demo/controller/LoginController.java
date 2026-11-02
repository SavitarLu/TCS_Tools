package com.example.demo.controller;

import com.example.demo.config.SessionKeys;
import com.example.demo.data.DUSER;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static com.example.demo.util.DB2Util.executeQuery;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               Model model,
                               HttpSession session) {
        if (username == null || username.trim().isEmpty() || password == null) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }

        // 使用占位符传参，避免 SQL 注入
        List<DUSER> duserList = executeQuery(
                "SELECT * FROM LS2WPRD.DUSER WHERE USER_ID = ?", DUSER.class, username.trim());

        if (duserList.isEmpty()) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }

        DUSER user = duserList.get(0);
        String storedPassword = user.getPasswd() == null ? "" : user.getPasswd().trim();
        if (!constantTimeEquals(password, storedPassword)) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }

        // 登录成功后写入会话，后续请求由 LoginInterceptor 校验
        session.setAttribute(SessionKeys.LOGIN_USER, user.getUserId());
        session.setAttribute(SessionKeys.LOGIN_USER_NAME, user.getUserName());
        model.addAttribute("username", user.getUserName());
        return "redirect:/report";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private static boolean constantTimeEquals(String actual, String expected) {
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
