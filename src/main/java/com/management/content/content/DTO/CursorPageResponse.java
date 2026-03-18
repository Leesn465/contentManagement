package com.management.content.content.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponse<C>(
        List<C> content,
        int size,
        boolean hasNext,
        LocalDateTime nextLastCreatedDate,
        Long nextLastId
) {
}
