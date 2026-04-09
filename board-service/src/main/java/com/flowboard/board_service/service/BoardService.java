package com.flowboard.board_service.service;

import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.util.CustomPageResponse;

public interface BoardService {
    public BoardResponseDto createBoard(BoardRequestDto dto, Integer userId);

    public BoardResponseDto updateBoard(Integer boardId, BoardRequestDto dto, Integer userId);

    public void deleteBoard(Integer boardId, Integer userId);

    public CustomPageResponse<BoardResponseDto> getBoardsByWorkspace(
            Integer workspaceId,
            Integer page,
            Integer size,
            String sort,
            String direction
    );

    public BoardResponseDto getBoardById(Integer boardId);

    public void closeBoard(Integer boardId, Integer userId);
}