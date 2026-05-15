package com.flowboard.card_service;

import com.flowboard.card_service.client.BoardClient;
import com.flowboard.card_service.client.WorkspaceClient;
import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.entity.ActivityType;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.CardActivity;
import com.flowboard.card_service.exception.IllegalOperationException;
import com.flowboard.card_service.mapper.impl.CardActivityResponseMapper;
import com.flowboard.card_service.repository.CardActivityRepository;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.service.impl.CardActivityServiceImpl;
import com.flowboard.card_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardActivityServiceImplTest {

    @Mock
    private CardActivityRepository cardActivityRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardClient boardClient;

    @Mock
    private WorkspaceClient workspaceClient;

    @Mock
    private CardActivityResponseMapper responseMapper;

    @InjectMocks
    private CardActivityServiceImpl cardActivityService;

    private Card getCard() {
        Card card = new Card();
        card.setCardId(1);
        card.setBoardId(10);
        return card;
    }

    @Test
    void logActivity_positive() {

        CardActivity activity = new CardActivity();

        when(cardActivityRepository.save(any(CardActivity.class)))
                .thenReturn(activity);

        when(responseMapper.mapTo(activity))
                .thenReturn(new CardActivityResponseDto());

        CardActivityResponseDto result =
                cardActivityService.logActivity(
                        1,
                        1,
                        ActivityType.CREATED,
                        "created"
                );

        assertNotNull(result);
    }

    @Test
    void getActivitiesByCard_publicBoard_returnsActivities() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10))
                .thenReturn(false);

        when(boardClient.getWorkspaceId(10))
                .thenReturn(100);

        when(workspaceClient.isPrivate(100))
                .thenReturn(false);

        Page<CardActivity> page =
                new PageImpl<>(
                        List.of(new CardActivity()),
                        PageRequest.of(0, 5),
                        1
                );

        when(cardActivityRepository.findByCardIdOrderByCreatedAtDesc(
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        when(responseMapper.mapTo(any(CardActivity.class)))
                .thenReturn(new CardActivityResponseDto());

        CustomPageResponse<CardActivityResponseDto> result =
                cardActivityService.getActivitiesByCard(
                        1,
                        0,
                        5,
                        "createdAt",
                        "asc",
                        1
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActivitiesByCard_privateBoardAndNotMember_throwsException() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10))
                .thenReturn(true);

        when(boardClient.isMember(10, 1))
                .thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> cardActivityService.getActivitiesByCard(
                        1,
                        0,
                        5,
                        "createdAt",
                        "asc",
                        1
                ));
    }

    @Test
    void getActivitiesByCard_privateBoardAndMember_returnsActivities() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10))
                .thenReturn(true);

        when(boardClient.isMember(10, 1))
                .thenReturn(true);

        Page<CardActivity> page =
                new PageImpl<>(
                        List.of(new CardActivity()),
                        PageRequest.of(0, 5),
                        1
                );

        when(cardActivityRepository.findByCardIdOrderByCreatedAtDesc(
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        when(responseMapper.mapTo(any(CardActivity.class)))
                .thenReturn(new CardActivityResponseDto());

        CustomPageResponse<CardActivityResponseDto> result =
                cardActivityService.getActivitiesByCard(
                        1,
                        0,
                        5,
                        "createdAt",
                        "desc",
                        1
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActivitiesByCard_privateWorkspaceAndNotMember_throwsException() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10))
                .thenReturn(false);

        when(boardClient.getWorkspaceId(10))
                .thenReturn(100);

        when(workspaceClient.isPrivate(100))
                .thenReturn(true);

        when(workspaceClient.isMember(100, 1))
                .thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> cardActivityService.getActivitiesByCard(
                        1,
                        0,
                        5,
                        "createdAt",
                        "asc",
                        1
                ));
    }

    @Test
    void getActivitiesByCard_privateWorkspaceAndMember_returnsActivities() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10))
                .thenReturn(false);

        when(boardClient.getWorkspaceId(10))
                .thenReturn(100);

        when(workspaceClient.isPrivate(100))
                .thenReturn(true);

        when(workspaceClient.isMember(100, 1))
                .thenReturn(true);

        Page<CardActivity> page =
                new PageImpl<>(
                        List.of(new CardActivity()),
                        PageRequest.of(0, 5),
                        1
                );

        when(cardActivityRepository.findByCardIdOrderByCreatedAtDesc(
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        when(responseMapper.mapTo(any(CardActivity.class)))
                .thenReturn(new CardActivityResponseDto());

        CustomPageResponse<CardActivityResponseDto> result =
                cardActivityService.getActivitiesByCard(
                        1,
                        0,
                        5,
                        "createdAt",
                        "asc",
                        1
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActivitiesByCard_whenCardMissing_throwsException() {

        when(cardRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(IllegalOperationException.class,
                () -> cardActivityService.getActivitiesByCard(
                        99,
                        0,
                        5,
                        "createdAt",
                        "asc",
                        1
                ));
    }
}