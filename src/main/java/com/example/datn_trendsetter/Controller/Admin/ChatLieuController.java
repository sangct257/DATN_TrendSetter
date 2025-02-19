package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Repository.ChatLieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatLieuController {

    @RequestMapping("admin/chat-lieu")
    public String chatLieu() {
        return "Admin/ChatLieu/hien-thi";
    }


}
