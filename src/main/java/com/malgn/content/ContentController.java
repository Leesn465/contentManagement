package com.malgn.content;

import com.malgn.auth.PrincipalDetails;
import com.malgn.content.DTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Content", description = "콘텐츠 관리 API")
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "콘텐츠 등록", description = "로그인한 사용자가 콘텐츠를 등록합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentResponse create(
            @RequestBody @Valid ContentCreateRequest request,
            @AuthenticationPrincipal
            PrincipalDetails userDetails
    ) {
        return contentService.create(request, userDetails);
    }

    @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ContentResponse getDetail(@PathVariable Long id) {
        return contentService.getDetail(id);
    }

    @Operation(summary = "콘텐츠 수정", description = "작성자 본인 또는 ADMIN만 수정 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ContentResponse update(
            @PathVariable Long id,
            @RequestBody @Valid ContentUpdateRequest request,
            @AuthenticationPrincipal
            PrincipalDetails userDetails
    ) {
        return contentService.update(id, request, userDetails);
    }

    @Operation(summary = "콘텐츠 삭제", description = "작성자 본인 또는 ADMIN만 삭제 가능합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal
            PrincipalDetails userDetails
    ) {
        contentService.delete(id, userDetails);
    }

    @Operation(summary = "콘텐츠 목록 조회 (Cursor 기반)")
    @GetMapping
    public CursorPageResponse<ContentResponse> getList(
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(required = false) LocalDateTime lastCreatedDate,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    ) {
        ContentCursorRequest request =
                new ContentCursorRequest(lastCreatedDate, lastId, size);

        return contentService.getListWithCursor(request);
    }





}
