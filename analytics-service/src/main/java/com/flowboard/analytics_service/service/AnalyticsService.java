package com.flowboard.analytics_service.service;

import com.flowboard.analytics_service.dto.*;
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
    WorkspaceAnalyticsResponseDto getWorkspaceAnalytics(Integer workspaceId, Integer userId);

    /*
     * Returns full analytics for a single member inside a workspace.
     */
    MemberAnalyticsDto getMemberAnalytics(Integer workspaceId, Integer memberId, Integer userId);

    /*
     * Returns full analytics for a single board inside a workspace.
     */
    BoardAnalyticsDto getBoardAnalytics(Integer workspaceId, Integer boardId, Integer userId);

    /*
     * Returns filtered cards for a workspace — all boards combined.
     * status (TO_DO/IN_PROGRESS/IN_REVIEW/DONE) and due (TODAY/THIS_WEEK/OVERDUE),
     */
    List<CardDto> getWorkspaceCards(Integer workspaceId, Integer userId,
                                    String status, String due, Integer assigneeId);

    /*
     * Returns filtered cards for a single board.
     */
    List<CardDto> getBoardCards(Integer workspaceId, Integer boardId, Integer userId,
                                String status, String due, Integer assigneeId);

    /*
     * Returns filtered cards assigned to a specific member inside a workspace.
     */
    List<CardDto> getMemberCards(Integer workspaceId, Integer memberId, Integer userId,
                                 String status, String due);
}
