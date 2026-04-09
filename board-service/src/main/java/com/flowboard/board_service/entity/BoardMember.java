package com.flowboard.board_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"boardId", "userId"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer boardMemberId;

    @Column(nullable = false)
    private Integer boardId;

    @Column(nullable = false)
    private Integer userId;

    /*
    Not using for now but each member of a board have 3 different role
    here @Column(nullable=false) is not used as we are not working with this now
     */
    @Enumerated(EnumType.STRING)
    private BoardRole role;

    @CreationTimestamp
    private LocalDateTime addedAt;
}