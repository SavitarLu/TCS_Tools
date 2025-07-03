package com.example.demo.controller;

import com.example.demo.data.DUSER;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.example.demo.util.DB2Util.*;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {
        String sql = "SELECT * FROM LS2WPRD.DUSER WHERE USER_ID = '" + username + "'";

        List<DUSER> duserList = executeQuery(sql, DUSER.class);

        // 检查结果集是否为空
        if (duserList == null || duserList.isEmpty()) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }

        DUSER user = duserList.get(0);
        // 验证逻辑
        if (password.equals(user.getPasswd().trim().toString())) {
            model.addAttribute("username", user.getUserName());
            return "redirect:/report";
            //return "welcome";
        } else {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }
}