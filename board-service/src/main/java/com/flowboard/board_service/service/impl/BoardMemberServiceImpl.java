package com.flowboard.board_service.service.impl;

import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.entity.BoardRole;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.Mapper;
import com.flowboard.board_service.repository.BoardMemberRepository;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.BoardMemberService;
import com.flowboard.board_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardMemberServiceImpl implements BoardMemberService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final Mapper<BoardMemberRequestDto, BoardMember> requestMapper;
    private final Mapper<BoardMember, BoardMemberResponseDto> responseMapper;

    @Override
    public BoardMemberResponseDto addMember(BoardMemberRequestDto dto, Integer userId) {
        return null;
    }

    @Override
    public void removeMember(Integer boardId, Integer memberUserId, Integer userId) {

    }

    @Override
    public CustomPageResponse<BoardMemberResponseDto> getMembers(Integer boardId, Integer userId, Integer page, Integer size, String sort, String direction) {
        return null;
    }
}