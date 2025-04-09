package com.example.datn_trendsetter.Controller.Admin;

import com.example.datn_trendsetter.Entity.ChatLieu;
import com.example.datn_trendsetter.Repository.ChatLieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatLieuController {

    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @RequestMapping("admin/chat-lieu")
    public String chatLieu(Model model) {
        model.addAttribute("chatLieu", chatLieuRepository.findByDeleted(false,Sort.by(Sort.Direction.DESC,"id")));
        return "Admin/ChatLieu/hien-thi";
    }


}
