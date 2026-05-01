package com.flowboard.analytics_service.service.impl;

import com.flowboard.analytics_service.client.BoardServiceClient;
import com.flowboard.analytics_service.client.CardServiceClient;
import com.flowboard.analytics_service.client.SubscriptionServiceClient;
import com.flowboard.analytics_service.client.WorkspaceServiceClient;
import com.flowboard.analytics_service.dto.*;
import com.flowboard.analytics_service.exception.IllegalOperationException;
import com.flowboard.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {
    private final WorkspaceServiceClient workspaceServiceClient;
    private final SubscriptionServiceClient subscriptionServiceClient;
    private final BoardServiceClient boardServiceClient;
    private final CardServiceClient cardServiceClient;

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
                    WorkspaceDto workspaceDto = (WorkspaceDto)ws;
                    Mono<Integer> boards =
                            boardServiceClient.getTotalBoardsByWorkspace(ws.getWorkspaceId());

                    Mono<Integer> members =
                            workspaceServiceClient.getTotalMemberInWorkspace(ws.getWorkspaceId());

                    Mono<CardStatusSummaryDto> cards =
                            cardServiceClient.getCardSummaryForWorkspace(ws.getWorkspaceId());

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

    @Override
    public WorkspaceAnalyticsResponseDto getWorkspaceAnalytics(Integer workspaceId, Integer userId) {
        return null;
    }

    @Override
    public MemberAnalyticsDto getMemberAnalytics(Integer workspaceId, Integer memberId, Integer userId) {
        return null;
    }

    @Override
    public BoardAnalyticsDto getBoardAnalytics(Integer workspaceId, Integer boardId, Integer userId) {
        return null;
    }

    @Override
    public List<CardDto> getWorkspaceCards(Integer workspaceId, Integer userId, String status, String due, Integer assigneeId) {
        return null;
    }

    @Override
    public List<CardDto> getBoardCards(Integer workspaceId, Integer boardId, Integer userId, String status, String due, Integer assigneeId) {
        return null;
    }

    @Override
    public List<CardDto> getMemberCards(Integer workspaceId, Integer memberId, Integer userId, String status, String due) {
        return null;
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
}
