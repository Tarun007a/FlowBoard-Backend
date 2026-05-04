package com.flowboard.analytics_service.service.impl;

import com.flowboard.analytics_service.client.*;
import com.flowboard.analytics_service.dto.*;
import com.flowboard.analytics_service.enums.CardStatus;
import com.flowboard.analytics_service.enums.DueFilter;
import com.flowboard.analytics_service.exception.IllegalOperationException;
import com.flowboard.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {
    private final WorkspaceServiceClient workspaceServiceClient;
    private final SubscriptionServiceClient subscriptionServiceClient;
    private final BoardServiceClient boardServiceClient;
    private final CardServiceClient cardServiceClient;
    private final ListServiceClient listServiceClient;
    private final AuthServiceClient authServiceClient;

    /*
    then + zip Together
    checkSubscription(userId)
    .then(
        Mono.zip(
            getUser(),
            getBoards(),
            getCards()
        )
    )
    Subscribe to outer pipeline, Run checkSubscription If success: subscribe to zip
    zip starts:
      getUser()
      getBoards()
      getCards()
    Wait for all three, Emit tuple and map tuple to response
    If One Zip Member Fails, zip emits error immediately whole pipeline fails
     */
    @Override
    public Mono<List<WorkspaceOverviewDto>> getUserAnalytics(Integer userId) {
        log.info("User analytics requested for userId={}", userId);

        return checkSubscription(userId)
                .then(workspaceServiceClient.getWorkspacesByUser(userId))
                .flatMapMany(Flux::fromIterable)
                .flatMap(ws -> {
                    Mono<Integer> boards =
                            boardServiceClient.getTotalBoardsByWorkspace(ws.getWorkspaceId());

                    Mono<Integer> members =
                            workspaceServiceClient.getTotalMemberInWorkspace(ws.getWorkspaceId());

                    Mono<CardStatusSummaryDto> cards =
                            cardServiceClient.getCardSummaryForWorkspace(ws.getWorkspaceId());
                    log.info("Analytics ready for user");
                    return Mono.zip(boards, members, cards)
                            .map(tuple -> {
                                WorkspaceOverviewDto dto = new WorkspaceOverviewDto();
                                dto.setWorkspaceId(ws.getWorkspaceId());
                                dto.setName(ws.getName());
                                dto.setTotalBoards(tuple.getT1());
                                dto.setTotalMembers(tuple.getT2());
                                dto.setCardsSummary(tuple.getT3());
                                return dto;
                            });
                })
                .collectList();
    }

    /*
        .then() only accepts single mono so use mono.zip and the call each service then use
        .map and get all the results.
     */
    @Override
    public Mono<WorkspaceAnalyticsResponseDto> getWorkspaceAnalytics(Integer workspaceId, Integer userId) {
        log.info("Workspace analytics requested for workspaceId={} userId={}", workspaceId, userId);

        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(
                        Mono.zip(
                                workspaceServiceClient.getWorkspaceById(workspaceId),
                                boardServiceClient.getTotalBoardsByWorkspace(workspaceId),
                                listServiceClient.getTotalListByWorkspace(workspaceId),
                                workspaceServiceClient.getTotalMemberInWorkspace(workspaceId),
                                cardServiceClient.getCardSummaryForWorkspace(workspaceId)))
                .map(tuple -> {
                    WorkspaceDto ws = tuple.getT1();

                    WorkspaceAnalyticsResponseDto dto = new WorkspaceAnalyticsResponseDto();
                    dto.setWorkspaceId(ws.getWorkspaceId());
                    dto.setName(ws.getName());
                    dto.setDescription(ws.getDescription());
                    dto.setVisibility(ws.getVisibility());
                    dto.setCreatedAt(ws.getCreatedAt());
                    dto.setTotalBoards(tuple.getT2());
                    dto.setTotalLists(tuple.getT3());
                    dto.setTotalMembers(tuple.getT4());
                    dto.setCardsSummary(tuple.getT5());

                    log.info("Workspace analytics ready for workspace {}", workspaceId);
                    return dto;
                });
    }

    @Override
    public Mono<MemberAnalyticsDto> getMemberAnalytics(Integer workspaceId, Integer memberId, Integer userId) {
        log.info("Member analytics requested for workspaceId={} memberId={} userId={}", workspaceId, memberId, userId);

        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(
                        Mono.zip(
                                authServiceClient.getUserById(memberId),
                                cardServiceClient.getCardSummaryForUser(workspaceId, memberId)
                        )
                )
                .map(tuple -> {
                    UserDto user = tuple.getT1();
                    CardStatusSummaryDto summary = tuple.getT2();

                    MemberAnalyticsDto dto = new MemberAnalyticsDto();
                    dto.setUserId(user.getUserId());
                    dto.setName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setProfilePicture(user.getAvatarUrl());
                    dto.setTotalPending(summary.getToDo() + summary.getInProgress());
                    dto.setCardsSummary(summary);
                    return dto;
                });
    }


    @Override
    public Mono<BoardAnalyticsDto> getBoardAnalytics(Integer workspaceId, Integer boardId, Integer userId) {
        log.info("Board analytics requested for workspaceId={} boardId={} userId={}", workspaceId, boardId, userId);

        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(
                        Mono.zip(
                                boardServiceClient.getBoardById(boardId),
                                listServiceClient.getTotalListsByBoard(boardId),
                                cardServiceClient.getCardSummaryForBoard(boardId)
                        )
                )
                .map(tuple -> {
                    BoardDto board = tuple.getT1();

                    BoardAnalyticsDto dto = new BoardAnalyticsDto();
                    dto.setBoardId(board.getBoardId());
                    dto.setName(board.getName());
                    dto.setVisibility(board.getVisibility());
                    dto.setIsClosed(board.getIsClosed());
                    dto.setTotalLists(tuple.getT2());
                    dto.setCardsSummary(tuple.getT3());
                    return dto;
                });
    }

    @Override
    public Mono<List<WorkspaceMemberDto>> getWorkspaceMembers(Integer workspaceId, Integer userId) {
        log.info("Get Workspace members");
        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(workspaceServiceClient.getMembers(workspaceId));
    }


    @Override
    public Mono<List<BoardDto>> getWorkspaceBoards(Integer workspaceId, Integer userId) {
        log.info("Get Workspace boards");
        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(boardServiceClient.getBoards(workspaceId));
    }

    @Override
    public Mono<List<ListDto>> getBoardLists(Integer boardId, Integer workspaceId, Integer userId) {
        log.info("Get Board Lists");
        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(listServiceClient.getLists(boardId));
    }

    @Override
    public Mono<List<CardDto>> getCards(Integer workspaceId, Integer boardId, Integer userId, CardStatus status, DueFilter due, Integer assigneeId) {
        log.info("Get cards for workspaceId={}, boardId={}, userId={}, status={}, due={}, assigneeId={}",
                workspaceId, boardId, userId, status, due, assigneeId);

        return checkSubscription(userId)
                .then(checkWorkspaceOwnership(workspaceId, userId))
                .then(cardServiceClient.getCardsByWorkspace(workspaceId))
                .map(cards -> cards.stream()
                        .filter(card -> {
                            return (boardId == null) || card.getBoardId().equals(boardId);
                        })

                        .filter(card -> {
                            return(assigneeId == null) || card.getAssigneeId().equals(assigneeId);
                        })

                        .filter(card -> (status == null) || card.getStatus().equals(status.toString()))

                        .filter(card -> applyDueFilter(card, due))

                        .toList()
                );
    }

    private boolean applyDueFilter(CardDto card, DueFilter due) {
        if (due == null) return true;

        if (card.getDueDate() == null) return false;
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();

        if (due == DueFilter.TODAY) {
            LocalDateTime endOfToday = startOfToday.plusDays(1).minusNanos(1);
            return !card.getDueDate().isBefore(startOfToday) && !card.getDueDate().isAfter(endOfToday);
        }

        else if (due == DueFilter.THIS_WEEK) {
            LocalDateTime endOfWeek = now.plusDays(7);

            return !card.getDueDate().isBefore(startOfToday)
                    && !card.getDueDate().isAfter(endOfWeek);
        }

        else if (due == DueFilter.OVERDUE) {
            return card.getDueDate().isBefore(now);
        }

        return true;
    }

    /*
                Mono<T>
                Do some async work
                If success -> complete and emit value of type T and 0 value for Mono<Void>/empty
                If failure -> emit error
                (here we say if success emit value as this is reactive the value may come later, async)

                This method builds are pipeline not execute, This constructs a chain describing:
                ex : This is only a recipe, not execution yet.
                Mono<String> mono =
                getUser()
                  .map(...)
                  .flatMap(...)
                  .timeout(...)

                call subscription service when boolean arrives, if false -> error, if true -> complete empty

                It does not execute immediately by itself.Reactive streams are lazy.

                monoA.then(...)
                this means subscribe to monoA, wait until monoA completes successfully

                // This is non-blocking, async and parallel execution
                Mono.zip(mono1, mono2, mono3)
                subscribe to all Monos, run them concurrently (logically in parallel async I/O), wait until all emit one value
                combine into tuple, emit tuple and done


                One way is to use block just after calling a method and give up the webclient advantages
                of non blocking and async, subscriptionServiceClient.isUserSubscribed(userId).block();
                 */
    private Mono<Void> checkSubscription(Integer userId) {
        log.debug("Checking subscription for userId={}", userId);
        return subscriptionServiceClient.isUserSubscribed(userId)
                .flatMap(subscribed -> {

                    if (Boolean.FALSE.equals(subscribed)) {
                        log.warn("Analytics access denied for userId={} not subscribed", userId);
                        return Mono.error(new IllegalOperationException("You need an active subscription to access analytics"));
                    }

                    log.debug("Subscription check passed for userId={}", userId);
                    return Mono.empty();
                });
    }

    private Mono<Void> checkWorkspaceOwnership(Integer workspaceId, Integer userId) {
        return workspaceServiceClient.isOwner(workspaceId, userId)
                .flatMap(bool -> {
                    if(!Boolean.TRUE.equals(bool)) return Mono.error(new IllegalOperationException("You are not the owner of workspace"));
                    return Mono.empty();
                });
    }
}
