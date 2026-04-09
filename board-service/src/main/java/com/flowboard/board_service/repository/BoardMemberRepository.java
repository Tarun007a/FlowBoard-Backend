package com.flowboard.board_service.repository;

import com.flowboard.board_service.entity.BoardMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer> {
    boolean existsByBoardIdAndUserId(Integer boardId, Integer userId);

    void deleteByBoardIdAndUserId(Integer boardId, Integer userId);

    Page<BoardMember> findByBoardId(Integer boardId, Pageable pageable);
}