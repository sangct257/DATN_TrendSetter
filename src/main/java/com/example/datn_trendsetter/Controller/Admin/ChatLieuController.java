package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Repository.ChatLieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatLieuController {


    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @RequestMapping("admin/chat-lieu")
    public String chatLieu() {
        return "/admin/chat-lieu";
    }

    @GetMapping("/api/chat-lieu")
    public List<ChatLieu> getChatLieu() {
        return chatLieuRepository.findAll();
    }
}
