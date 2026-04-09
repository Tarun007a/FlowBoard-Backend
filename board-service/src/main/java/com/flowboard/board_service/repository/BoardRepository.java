package com.flowboard.board_service.repository;

import com.flowboard.board_service.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    Page<Board> findByWorkspaceId(Integer workspaceId, Pageable pageable);

    boolean existsByNameAndWorkspaceId(String name, Integer workspaceId);
}