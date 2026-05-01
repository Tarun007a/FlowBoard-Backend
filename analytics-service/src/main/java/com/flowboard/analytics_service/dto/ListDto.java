package com.flowboard.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListDto {
    private Integer listId;

    private Integer boardId;

    private String name;

    private Integer position;

}
