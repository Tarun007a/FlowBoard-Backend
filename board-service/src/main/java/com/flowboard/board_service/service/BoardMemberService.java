package com.flowboard.board_service.service;

import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.util.CustomPageResponse;

public interface BoardMemberService {

    public BoardMemberResponseDto addMember(BoardMemberRequestDto dto, Integer userId);

    public void removeMember(Integer boardId, Integer memberUserId, Integer userId);

    public CustomPageResponse<BoardMemberResponseDto> getMembers(
            Integer boardId,
            Integer userId,
            Integer page,
            Integer size,
            String sort,
            String direction
    );
}