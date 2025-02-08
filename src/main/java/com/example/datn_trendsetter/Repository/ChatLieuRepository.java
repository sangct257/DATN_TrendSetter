package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu,Integer> {
    boolean existsByTenChatLieu(String tenChatLieu);
}
