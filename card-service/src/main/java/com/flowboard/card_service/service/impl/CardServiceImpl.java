package com.flowboard.card_service.service.impl;

import com.flowboard.card_service.client.BoardClient;
import com.flowboard.card_service.client.ListClient;
import com.flowboard.card_service.client.WorkspaceClient;
import com.flowboard.card_service.dto.*;
import com.flowboard.card_service.entity.ActivityType;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.exception.CardNotFoundException;
import com.flowboard.card_service.exception.IllegalOperationException;
import com.flowboard.card_service.mapper.Mapper;
import com.flowboard.card_service.mapper.impl.CardRequestMapper;
import com.flowboard.card_service.mapper.impl.CardResponseMapper;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.service.CardActivityService;
import com.flowboard.card_service.service.CardService;
import com.flowboard.card_service.service.NotificationProcedure;
import com.flowboard.card_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardRequestMapper requestMapper;
    private final CardResponseMapper responseMapper;
    private final WorkspaceClient workspaceClient;
    private final BoardClient boardClient;
    private final ListClient listClient;
    private final NotificationProcedure notificationProducer;
    private final CardActivityService cardActivityService;

    private void sendNotification(Integer recipientId,
                                  Integer actorId,
                                  NotificationType type,
                                  String title,
                                  String message,
                                  Integer relatedId) {

        if(recipientId == null) return;

        NotificationRequestDto dto = new NotificationRequestDto();

        dto.setRecipientId(recipientId);
        dto.setActorId(actorId);
        dto.setNotificationType(type);
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setRelatedId(relatedId);
        dto.setRelatedType(RelatedType.CARD);

        notificationProducer.sendSingle(dto);
    }

    @Override
    public CardResponseDto createCard(CardRequestDto cardRequestDto, Integer userId) {
        Card card = requestMapper.mapTo(cardRequestDto);

        Integer boardId = card.getBoardId();

        Integer listId = card.getListId();
        Integer assigneeId = card.getAssigneeId();

        validateModificationRequest(boardId, userId);
        validateModificationRequest(boardId, assigneeId);

        Integer lastPosition = getMaxPosition(listId);

        card.setPosition(lastPosition+1);
        card.setCoverColor(getCoverColor(card.getPriority()));
        card.setCreatedById(userId);

        Card savedCard = cardRepository.save(card);
        cardActivityService.logActivity(
                savedCard.getCardId(),
                userId,
                ActivityType.CREATED,
                "Created card '" + savedCard.getTitle() + "'"
        );

        sendNotification(
                card.getAssigneeId(),
                userId,
                NotificationType.ASSIGNMENT,
                "Card Created and  Assigned",
                "New card created: " + card.getTitle() + " and assigned to you",
                card.getCardId()
        );

        return responseMapper.mapTo(savedCard);
    }

    @Override
    public CardResponseDto getCardById(Integer cardId, Integer userId) {
        Card card = getCard(cardId);

        validateViewRequest(card.getBoardId(), userId);

        return responseMapper.mapTo(card);
    }

    @Override
    public List<CardResponseDto> getCardsByList(Integer listId, Integer userId) {
        List<Card> cards = cardRepository.findByListIdAndIsArchivedFalseOrderByPosition(listId);
        if(cards.isEmpty()) throw new CardNotFoundException("No such card exception");

        Integer boardId = cards.get(0).getBoardId();
        validateViewRequest(boardId, userId);

        return cards
                .stream()
                .map(responseMapper::mapTo)
                .toList();
    }

    @Override
    public List<CardResponseDto> getCardsByBoard(Integer boardId, Integer userId) {
        validateViewRequest(boardId, userId);

        List<Card> cards = cardRepository.findByBoardIdOrderByPosition(boardId);
        return cards
                .stream()
                .map(responseMapper::mapTo)
                .toList();
    }

    @Override
    public List<CardResponseDto> getCardsByAssignee(Integer assigneeId, Integer userId) {
        List<Card> cards = cardRepository.findByAssigneeId(assigneeId);

        if(cards.isEmpty()) throw new CardNotFoundException("No cards found");

        Integer boardId = cards.get(0).getBoardId();
        validateViewRequest(boardId, userId);

        return cards.stream()
                .map(responseMapper::mapTo)
                .toList();
    }

    @Override
    public CardResponseDto updateCard(Integer cardId, CardUpdateDto cardUpdateDto, Integer userId) {
        log.info("Update card requested for card {} by user {}", cardId, userId);
        Card card = getCard(cardId);

        validateModificationRequest(card.getBoardId(), userId);

        card.setTitle(cardUpdateDto.getTitle());
        card.setDescription(cardUpdateDto.getDescription());
        card.setPriority(cardUpdateDto.getPriority());
        card.setStartDate(cardUpdateDto.getStartDate());
        card.setDueDate(cardUpdateDto.getDueDate());
        card.setStatus(cardUpdateDto.getStatus());

        Card updatedCard = cardRepository.save(card);
        cardActivityService.logActivity(
                updatedCard.getCardId(),
                userId,
                ActivityType.UPDATED,
                "Updated card '" + updatedCard.getTitle() + "'"
        );

        return responseMapper.mapTo(updatedCard);
    }

    @Override
    public void deleteCard(Integer cardId, Integer userId) {
        log.info("Delete card requested for card {} by user {}", cardId, userId);
        Card card = getCard(cardId);

        validateModificationRequest(card.getBoardId(), userId);

        cardRepository.delete(card);
        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.DELETED,
                "Deleted card '" + card.getTitle() + "'"
        );
    }

    @Override
    @Transactional
    public CardResponseDto moveCard(Integer cardId, Integer targetListId, Integer newPosition, Integer userId) {
        log.info("Move card requested for card {} to list {} by user {}", cardId, targetListId, userId);
        Card card = getCard(cardId);
        Integer sourceListId = card.getListId();
        Integer boardId = card.getBoardId();

        validateModificationRequest(boardId, userId);

        Integer targetBoardId = listClient.getBoardId(targetListId);

        if(!boardId.equals(targetBoardId)) {
            throw new IllegalOperationException("Cannot move card across different boards");
        }

        List<Card> targetCards = cardRepository
                .findByListIdAndIsArchivedFalseOrderByPosition(targetListId);

        if(newPosition < 1 || newPosition > targetCards.size() + 1) {
            throw new IllegalOperationException("Invalid position");
        }

        List<Card> sourceCards = cardRepository
                .findByListIdAndIsArchivedFalseOrderByPosition(sourceListId);

        sourceCards.remove(getCard(cardId));

        for(int i = 0; i < sourceCards.size(); i++) {
            sourceCards.get(i).setPosition(i + 1);
        }

        card.setListId(targetListId);
        targetCards.add(newPosition - 1, card);

        for(int i = 0; i < targetCards.size(); i++) {
            targetCards.get(i).setPosition(i + 1);
        }

        cardRepository.saveAll(sourceCards);
        cardRepository.saveAll(targetCards);

        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.MOVED,
                "Moved card to list " + targetListId +
                        " at position " + newPosition
        );

        sendNotification(
                card.getAssigneeId(),
                userId,
                NotificationType.MOVE,
                "Card Moved",
                "Card '" + card.getTitle() + "' moved",
                card.getCardId()
        );

        return responseMapper.mapTo(card);
    }

    @Override
    @Transactional
    public void reorderCards(Integer listId, List<Integer> orderedCardIds, Integer userId) {
        log.info("Card reorder requested for list {} by user {}", listId, userId);
        List<Card> cards = cardRepository
                .findByListIdAndIsArchivedFalseOrderByPosition(listId);

        if(cards.size() != orderedCardIds.size()) {
            throw new IllegalOperationException("Mismatch in card count");
        }

        Integer boardId = cards.get(0).getBoardId();
        validateModificationRequest(boardId, userId);

        Map<Integer, Card> map = cards.stream()
                .collect(Collectors.toMap(Card::getCardId, c -> c));

        for(int i = 0; i < orderedCardIds.size(); i++) {
            Integer cardId = orderedCardIds.get(i);

            if(!map.containsKey(cardId)) {
                throw new IllegalOperationException("Invalid card id");
            }

            map.get(cardId).setPosition(i + 1);
        }

        cardRepository.saveAll(cards);
        for(Integer cardId : orderedCardIds) {

            cardActivityService.logActivity(
                    cardId,
                    userId,
                    ActivityType.REORDERED,
                    "Reordered card in list " + listId
            );
        }
    }

    @Override
    public CardResponseDto assignCard(Integer cardId, Integer assigneeId, Integer userId) {
        log.info("Assign card requested for card {} to user {} by user {}", cardId, assigneeId, userId);
        Card card = getCard(cardId);
        Integer boardId = card.getBoardId();

        validateModificationRequest(boardId, userId);
        validateModificationRequest(boardId, assigneeId);

        card.setAssigneeId(assigneeId);

        Card updatedCard = cardRepository.save(card);

        sendNotification(
                assigneeId,
                userId,
                NotificationType.ASSIGNMENT,
                "Card Assigned",
                "You have been assigned: " + card.getTitle(),
                card.getCardId()
        );

        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.ASSIGNED,
                "Assigned card to user " + assigneeId
        );

        return responseMapper.mapTo(updatedCard);
    }

    @Override
    public CardResponseDto updatePriority(Integer cardId, Priority priority, Integer userId) {
        log.info("Priority update requested for card {} by user {}", cardId, userId);
        Card card = getCard(cardId);
        Integer boardId = card.getBoardId();

        validateModificationRequest(boardId, userId);

        card.setPriority(priority);

        Card updatedCard = cardRepository.save(card);

        sendNotification(
                card.getAssigneeId(),
                userId,
                NotificationType.SYSTEM,
                "Priority Updated",
                "Priority of '" + card.getTitle() + "' changed to " + priority,
                card.getCardId()
        );

        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.PRIORITY_CHANGED,
                "Priority changed to " + priority
        );

        return responseMapper.mapTo(updatedCard);
    }

    @Override
    public CardResponseDto updateStatus(Integer cardId, Status status, Integer userId) {
        log.info("Status update requested for card {} by user {}", cardId, userId);
        Card card = getCard(cardId);
        Integer boardId = card.getBoardId();

        validateModificationRequest(boardId, userId);

        card.setStatus(status);

        Card updated = cardRepository.save(card);

        sendNotification(
                card.getAssigneeId(),
                userId,
                NotificationType.SYSTEM,
                "Status Updated",
                "Status of '" + card.getTitle() + "' changed to " + status,
                card.getCardId()
        );
        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.STATUS_CHANGED,
                "Status changed to " + status
        );

        return responseMapper.mapTo(updated);
    }

    @Override
    public void archiveCard(Integer cardId, Integer userId) {
        log.info("Archive card requested for card {} by user {}", cardId, userId);
        Card card = getCard(cardId);
        Integer boardId = card.getBoardId();

        validateModificationRequest(boardId, userId);

        card.setIsArchived(true);

        cardRepository.save(card);
        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.ARCHIVED,
                "Archived card '" + card.getTitle() + "'"
        );
    }

    @Override
    public void unarchiveCard(Integer cardId, Integer userId) {
        log.info("Unarchive card requested for card {} by user {}", cardId, userId);

        Card card = getCard(cardId);
        Integer boardId = card.getBoardId();

        validateModificationRequest(boardId, userId);

        card.setIsArchived(false);

        cardRepository.save(card);

        cardActivityService.logActivity(
                cardId,
                userId,
                ActivityType.UNARCHIVED,
                "Unarchived card '" + card.getTitle() + "'"
        );
    }

    @Override
    public List<CardResponseDto> getOverdueCards(Integer userId) {
        List<Card> overdue = cardRepository
                .findByAssigneeIdAndDueDateBeforeAndStatusNotOrderByPositionAsc(
                        userId,
                        LocalDateTime.now(),
                        Status.DONE
                );

        return overdue.stream()
                .map(responseMapper::mapTo)
                .toList();
    }

    @Override
    public Integer getAssignedUserId(Integer cardId) {
        return getCard(cardId).getAssigneeId();
    }

    @Override
    public CardStatusSummaryDto cardSummaryForWorkspace(Integer workspaceId) {

        log.info("Generating card summary for a workspace {}", workspaceId);

        List<Integer> boardIds = boardClient.getBoardIdByWorkspaceId(workspaceId);

        long toDo = cardRepository.countByBoardIdInAndStatus(boardIds, Status.TO_DO);

        long inProgress = cardRepository.countByBoardIdInAndStatus(boardIds, Status.IN_PROGRESS);

        long inReview = cardRepository.countByBoardIdInAndStatus(boardIds, Status.IN_REVIEW);

        long done = cardRepository.countByBoardIdInAndStatus(boardIds, Status.DONE);

        long overdueCards =
                cardRepository.countByBoardIdInAndDueDateBeforeAndStatusNot(
                        boardIds,
                        LocalDateTime.now(),
                        Status.DONE
                );

        long total = cardRepository.countByBoardIdIn(boardIds);

        long completionRate = 0;

        if (total > 0) {
            completionRate = (done * 100) / total;
        }

        return CardStatusSummaryDto.builder()
                .toDo(toDo)
                .overdueCards(overdueCards)
                .inProgress(inProgress)
                .inReview(inReview)
                .done(done)
                .total(total)
                .completionRate(completionRate)
                .build();
    }

    @Override
    public CardStatusSummaryDto cardSummaryForUser(Integer workspaceId, Integer userId) {
        log.info("Generating card summary for user {}", userId);

        List<Integer> boardIds = boardClient.getBoardIdByWorkspaceId(workspaceId);

        log.info(boardIds.toString());

        long toDo = cardRepository.countByBoardIdInAndAssigneeIdAndStatus(boardIds, userId, Status.TO_DO);

        long inProgress = cardRepository.countByBoardIdInAndAssigneeIdAndStatus(boardIds, userId, Status.IN_PROGRESS);

        long inReview = cardRepository.countByBoardIdInAndAssigneeIdAndStatus(boardIds, userId, Status.IN_REVIEW);

        long done = cardRepository.countByBoardIdInAndAssigneeIdAndStatus(boardIds, userId, Status.DONE);

        long overdueCards =
                cardRepository.countByBoardIdInAndAssigneeIdAndDueDateBeforeAndStatusNot(
                        boardIds,
                        userId,
                        LocalDateTime.now(),
                        Status.DONE
                );

        long total = cardRepository.countByBoardIdInAndAssigneeId(boardIds, userId);

        long completionRate = 0;

        if (total > 0) {
            completionRate = (done * 100) / total;
        }

        return CardStatusSummaryDto.builder()
                .toDo(toDo)
                .overdueCards(overdueCards)
                .inProgress(inProgress)
                .inReview(inReview)
                .done(done)
                .total(total)
                .completionRate(completionRate)
                .build();
    }

    @Override
    public CardStatusSummaryDto cardSummaryForBoard(Integer boardId) {
        log.info("Generating card summary for board {}", boardId);

        long toDo = cardRepository.countByBoardIdAndStatus(boardId, Status.TO_DO);

        long inProgress = cardRepository.countByBoardIdAndStatus(boardId, Status.IN_PROGRESS);

        long inReview = cardRepository.countByBoardIdAndStatus(boardId, Status.IN_REVIEW);

        long done = cardRepository.countByBoardIdAndStatus(boardId, Status.DONE);

        long overdueCards =
                cardRepository.countByBoardIdAndDueDateBeforeAndStatusNot(
                        boardId,
                        LocalDateTime.now(),
                        Status.DONE
                );

        long total = cardRepository.countByBoardId(boardId);

        long completionRate = 0;

        if (total > 0) {
            completionRate = (done * 100) / total;
        }

        return CardStatusSummaryDto.builder()
                .toDo(toDo)
                .overdueCards(overdueCards)
                .inProgress(inProgress)
                .inReview(inReview)
                .done(done)
                .total(total)
                .completionRate(completionRate)
                .build();
    }

    @Override
    public List<CardDto> findByWorkspace(Integer workspaceId) {
        log.info("Get all cards by workspace");

        List<Integer> boardIds = boardClient.getBoardIdByWorkspaceId(workspaceId);
        List<Card> cardList = cardRepository.findByBoardIdIn(boardIds);

        return cardList
                .stream()
                .map(card -> {
                    return CardDto
                            .builder()
                            .cardId(card.getCardId())
                            .listId(card.getListId())
                            .boardId(card.getBoardId())
                            .title(card.getTitle())
                            .description(card.getDescription())
                            .position(card.getPosition())
                            .priority(card.getPriority().toString())
                            .status(card.getStatus().toString())
                            .dueDate(card.getDueDate())
                            .startDate(card.getStartDate())
                            .assigneeId(card.getAssigneeId())
                            .createdById(card.getCreatedById())
                            .isArchived(card.getIsArchived())
                            .build();
                })
                .toList();
    }

    /*
                        if both the if conditions are false this means public workspace and public board
                        here we are making 3 calls to board service in worst case and 2 calls to workspace
                        service a better idea is to just make a single call and create a dto to take all
                        the things which to want in return for optimization(to do)
                */
    private void validateViewRequest(Integer boardId, Integer userId) {
        /*
         If board is private user must be member
        */
        if(boardClient.isPrivate(boardId)) {
            if(boardClient.isMember(boardId, userId)) return;
            throw new IllegalOperationException("You are not allowed to view this list");
        }

        /*
        If we are here means board is public so now if workspace is private you must
         be member of workspace
         */
        Integer workspaceId = boardClient.getWorkspaceId(boardId);
        if(workspaceClient.isPrivate(workspaceId)) {
            if(workspaceClient.isMember(workspaceId, userId)) return;
            throw new IllegalOperationException("You are not allowed to view this list");
        }

        // public board + public workspace -> allowed
    }

    /*
    Look at this how badly this is optimized this is making 3 calls to board service
    a better idea is to create a dto to take all the 3 things and just make a single
    call to board service for improvements -> to do if have time
     */
    private void validateModificationRequest(Integer boardId, Integer userId) {
        /*
         If board is private user must be member
        */
        if(boardClient.isPrivate(boardId)) {
            if(boardClient.isMember(boardId, userId)) return;
            throw new IllegalOperationException("User not allowed to modify this list");
        }

        /*
         If we are here means board is public so now must be member to make changes
        */
        Integer workspaceId = boardClient.getWorkspaceId(boardId);
        if(!workspaceClient.isMember(workspaceId, userId)){
            throw new IllegalOperationException("User not allowed to modify this list");
        }
    }

    private Integer getMaxPosition(Integer listId) {
        return cardRepository.maxPosition(listId);
    }

    private String getCoverColor(Priority priority) {
        if(priority == Priority.CRITICAL) return AppConstants.DEFAULT_COLOR_CRITICAL;
        if(priority == Priority.HIGH) return AppConstants.DEFAULT_COLOR_HIGH;
        if(priority == Priority.MEDIUM) return AppConstants.DEFAULT_COLOR_MEDIUM;
        return AppConstants.DEFAULT_COLOR_LOW;
    }

    private Card getCard(Integer cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }
}
