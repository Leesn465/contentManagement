package com.malgn.content;

import com.malgn.auth.PrincipalDetails;
import com.malgn.content.DTO.*;
import com.malgn.exception.BadRequestException;
import com.malgn.exception.ForbiddenException;
import com.malgn.exception.ResourceNotFoundException;
import com.malgn.user.Role;
import com.malgn.user.User;
import com.malgn.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 콘텐츠 서비스
 * - 콘텐츠 생성, 조회, 수정, 삭제 등 핵심 비즈니스 로직 처리
 * - 권한 검증 및 커서 기반 페이징 처리 포함
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    /**
     * 콘텐츠 생성
     * - 로그인 사용자 정보를 기반으로 작성자 설정
     */
    @Transactional
    public ContentResponse create(ContentCreateRequest request, PrincipalDetails userDetails) {
        User author = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("아이디" + userDetails.getId() + "찾을 수 없습니다."));

        Content content = Content.create(
                request.title(),
                request.description(),
                author
        );
        Content savedContent = contentRepository.save(content);
        return ContentResponse.from(savedContent);

    }

    /**
     * 콘텐츠 수정
     * - 작성자 또는 ADMIN 권한만 수정 가능
     */
    @Transactional
    public ContentResponse update(Long id, ContentUpdateRequest request, PrincipalDetails userDetails) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("콘텐츠를 찾을 수 없습니다"));

        validateCheck(content, userDetails);

        content.update(
                request.title(),
                request.description(),
                userDetails.getUsername()
        );
        return ContentResponse.from(content);

    }

    /**
     * 콘텐츠 삭제
     * - 작성자 또는 ADMIN 권한만 삭제 가능
     */
    @Transactional
    public void delete(Long id, PrincipalDetails userDetails) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("콘텐츠를 찾을 수 없습니다."));

        validateCheck(content, userDetails);

        contentRepository.delete(content);
    }

    /**
     * 콘텐츠 상세 조회
     * - 조회 시 viewCount 증가 처리
     */
    @Transactional
    public ContentResponse getDetail(Long id){
        contentRepository.increaseViewCount(id);
        return getDetailWithoutIncrease(id);
    }

    @Transactional(readOnly = true)
    public ContentResponse getDetailWithoutIncrease(Long id){
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("콘텐츠를 찾을 수 없습니다."));
        return ContentResponse.from(content);
    }


    /**
     * 커서 기반 콘텐츠 목록 조회
     * - size + 1 조회를 통해 다음 페이지 존재 여부 판단
     * - (createdDate, id)를 커서로 사용하여 안정적인 페이징 보장
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<ContentResponse> getListWithCursor(ContentCursorRequest request) {
        int size = request.size();

        if (size < 1 || size > 100) {
            throw new BadRequestException("size는 1 이상 100 이하여야 합니다.");
        }

        PageRequest pageable = PageRequest.of(0, size + 1);

        List<Content> contents;
        if (request.lastCreatedDate() == null && request.lastId() == null) {
            contents = contentRepository.findFirstPage(pageable);
        } else {
            contents = contentRepository.findNextPage(
                    request.lastCreatedDate(),
                    request.lastId(),
                    pageable
            );
        }

        boolean hasNext = contents.size() > size;
        List<Content> result = hasNext ? contents.subList(0, size) : contents;

        List<ContentResponse> responseList =
                result.stream().map(ContentResponse::from).toList();

        Content lastContent = result.isEmpty() ? null : result.getLast();

        return new CursorPageResponse<>(
                responseList,
                size,
                hasNext,
                lastContent != null ? lastContent.getCreatedDate() : null,
                lastContent != null ? lastContent.getId() : null
        );
    }


    /**
     * 권한 검증
     * - 작성자 본인 또는 ADMIN 권한만 허용
     */
    private void validateCheck(Content content, PrincipalDetails userDetails) {
        boolean isOwner = content.getAuthor().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("해당 콘텐츠에 대한 권한이 없습니다.");
        }
    }


}
