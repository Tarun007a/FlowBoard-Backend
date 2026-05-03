package com.flowboard.analytics_service.service;

import com.flowboard.analytics_service.dto.*;
import com.flowboard.analytics_service.enums.CardStatus;
import com.flowboard.analytics_service.enums.DueFilter;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AnalyticsService {

    /*
     So we are returning a mono to spring boot from controller so now what will happen,
     as the request comes dispatcher servlet -> Handler mapper -> Handler adapter ->
     then controller called(blocking) -> return dto -> message converter(jackson) ->
     convert to json -> return
     so in WebFlux Instead of DispatcherServlet spring web flux uses DispatcherHandler
     DispatcherHandler -> HandlerMapping -> HandlerAdapter -> controller Returns Mono ->
     (now till now nothing is executed only a reactive pipeline is built)
     spring subscribe(HandlerResultHandler specifically) -> async execution start -> Response emit -> Response written
     HandlerAdapter subscribes → pipeline starts
     Non-blocking HTTP calls via Netty
     Thread released immediately
     Data arrives, Event loop triggers continuation, flatMap / zip / map executed
     Mono emits List<WorkspaceOverviewDto> Instead of HttpMessageConverter uses HttpMessageWriter

     */
    Mono<List<WorkspaceOverviewDto>> getUserAnalytics(Integer userId);

    /*
     * Returns full workspace analytics
     */
    Mono<WorkspaceAnalyticsResponseDto> getWorkspaceAnalytics(Integer workspaceId, Integer userId);

    /*
     * Returns full analytics for a single member inside a workspace.
     */
    Mono<MemberAnalyticsDto> getMemberAnalytics(Integer workspaceId, Integer memberId, Integer userId);

    /*
     * Returns full analytics for a single board inside a workspace.
     */
    Mono<BoardAnalyticsDto> getBoardAnalytics(Integer workspaceId, Integer boardId, Integer userId);

    /*
     * Returns filtered cards for a workspace — all boards combined.
     * status (TO_DO/IN_PROGRESS/IN_REVIEW/DONE) and due (TODAY/THIS_WEEK/OVERDUE),
     */

    Mono<List<WorkspaceMemberDto>> getWorkspaceMembers(Integer workspaceId, Integer userId);

    Mono<List<BoardDto>> getWorkspaceBoards(Integer workspaceId, Integer userId);

    Mono<List<ListDto>> getBoardLists(Integer boardId, Integer workspaceId, Integer userId);

    Mono<List<CardDto>> getCards(Integer workspaceId, Integer boardId, Integer userId, CardStatus status, DueFilter due, Integer assigneeId);
}
