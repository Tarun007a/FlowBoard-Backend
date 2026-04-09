package com.flowboard.board_service.service.impl;

import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.Mapper;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.BoardService;
import com.flowboard.board_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final Mapper<BoardRequestDto, Board> boardRequestMapper;
    private final Mapper<Board, BoardResponseDto> boardResponseMapper;

    @Override
    public BoardResponseDto createBoard(BoardRequestDto boardRequestDto, Integer userId) {
        Board board = boardRequestMapper.mapTo(boardRequestDto);
        board.setCreatedById(userId);

    }

    @Override
    public BoardResponseDto updateBoard(Integer boardId, BoardRequestDto dto, Integer userId) {
        return null;
    }

    @Override
    public void deleteBoard(Integer boardId, Integer userId) {

    }

    @Override
    public CustomPageResponse<BoardResponseDto> getBoardsByWorkspace(Integer workspaceId, Integer page, Integer size, String sort, String direction) {
        return null;
    }

    @Override
    public BoardResponseDto getBoardById(Integer boardId) {
        return null;
    }

    @Override
    public void closeBoard(Integer boardId, Integer userId) {

    }

    private void validateAccess()
}