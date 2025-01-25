package com.example.datn_trendsetter.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatLieuController {


    @RequestMapping("admin/chat-lieu")
    public String chatLieu() {
        return "Admin/ChatLieu/hien-thi";
    }

}
