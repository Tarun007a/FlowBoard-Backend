package com.flowboard.card_service.controller;

import com.flowboard.card_service.dto.CardRequestDto;
import com.flowboard.card_service.dto.CardResponseDto;
import com.flowboard.card_service.dto.CardUpdateDto;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final CardService cardService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return Integer.parseInt(userIdHeader);
    }

    @PostMapping("/create")
    public ResponseEntity<CardResponseDto> createCard(@RequestBody CardRequestDto dto,
                                                      HttpServletRequest request) {
        Integer userId = extractUserId(request);
        CardResponseDto card = cardService.createCard(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(card);
    }

    @GetMapping("/get/{cardId}")
    public ResponseEntity<CardResponseDto> getCard(@PathVariable Integer cardId,
                                                   HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardById(cardId, userId));
    }

    @GetMapping("/get/list/{listId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByList(@PathVariable Integer listId,
                                                                HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardsByList(listId, userId));
    }

    @GetMapping("/get/board/{boardId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByBoard(@PathVariable Integer boardId,
                                                                 HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardsByBoard(boardId, userId));
    }

    @GetMapping("/get/assignee/{assigneeId}")
    public ResponseEntity<List<CardResponseDto>> getCardsByAssignee(@PathVariable Integer assigneeId,
                                                                    HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getCardsByAssignee(assigneeId, userId));
    }

    @PutMapping("/update/{cardId}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Integer cardId,
                                                      @RequestBody CardUpdateDto dto,
                                                      HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.updateCard(cardId, dto, userId));
    }

    @DeleteMapping("/delete/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable Integer cardId,
                                             HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.deleteCard(cardId, userId);
        return ResponseEntity.ok("Deleted successfully");
    }

    @PutMapping("/{cardId}/move")
    public ResponseEntity<CardResponseDto> moveCard(@PathVariable Integer cardId,
                                                    @RequestParam Integer targetListId,
                                                    @RequestParam Integer position,
                                                    HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.moveCard(cardId, targetListId, position, userId));
    }

    @PutMapping("/list/{listId}/reorder")
    public ResponseEntity<String> reorderCards(@PathVariable Integer listId,
                                               @RequestBody List<Integer> orderedCardIds,
                                               HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.reorderCards(listId, orderedCardIds, userId);
        return ResponseEntity.ok("Reordered successfully");
    }

    @PutMapping("/{cardId}/assign")
    public ResponseEntity<CardResponseDto> assignCard(@PathVariable Integer cardId,
                                                      @RequestParam Integer assigneeId,
                                                      HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.assignCard(cardId, assigneeId, userId));
    }

    @PutMapping("/{cardId}/priority")
    public ResponseEntity<CardResponseDto> updatePriority(@PathVariable Integer cardId,
                                                          @RequestParam Priority priority,
                                                          HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.updatePriority(cardId, priority, userId));
    }

    @PutMapping("/{cardId}/status")
    public ResponseEntity<CardResponseDto> updateStatus(@PathVariable Integer cardId,
                                                        @RequestParam Status status,
                                                        HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.updateStatus(cardId, status, userId));
    }

    @PutMapping("/{cardId}/archive")
    public ResponseEntity<String> archiveCard(@PathVariable Integer cardId,
                                              HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.archiveCard(cardId, userId);
        return ResponseEntity.ok("Archived");
    }

    @PutMapping("/{cardId}/unarchive")
    public ResponseEntity<String> unarchiveCard(@PathVariable Integer cardId,
                                                HttpServletRequest request) {
        Integer userId = extractUserId(request);
        cardService.unarchiveCard(cardId, userId);
        return ResponseEntity.ok("Unarchived");
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<CardResponseDto>> getOverdueCards(HttpServletRequest request) {
        Integer userId = extractUserId(request);
        return ResponseEntity.ok(cardService.getOverdueCards(userId));
    }
}