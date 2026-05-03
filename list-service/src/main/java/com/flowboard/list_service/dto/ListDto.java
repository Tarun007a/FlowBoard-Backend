package com.flowboard.list_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListDto {
    private Integer listId;

    private Integer boardId;

    private String name;

    private Integer position;

}
