package com.example.datn_trendsetter.Repository;

import com.example.datn_trendsetter.Entity.ChatLieu;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu,Integer> {
    boolean existsByTenChatLieu(String tenChatLieu);

    List<ChatLieu> findByDeleted(Boolean deleted,Sort sort);

    List<ChatLieu> findByTrangThai(String trangThai, Sort sort);

    Integer countByTrangThai(String trangThai);
}
