package com.gymsocial.shared.pagination;

import java.util.List;

public record CursorPage<T>(
    List<T> items,
    String nextCursor,
    boolean hasMore
) {
}
