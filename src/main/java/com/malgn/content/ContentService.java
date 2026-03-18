package com.malgn.content;

import com.malgn.auth.PrincipalDetails;
import com.malgn.content.DTO.*;
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

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

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
    @Transactional
    public void delete(Long id, PrincipalDetails userDetails) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("콘텐츠를 찾을 수 없습니다."));

        validateCheck(content, userDetails);

        contentRepository.delete(content);
    }

    @Transactional
    public ContentResponse getDetail(Long id){
        Content content = contentRepository.findById(id)
                .orElseThrow(() ->new ResourceNotFoundException("콘텐츠를 찾을 수 없습니다."));
        content.increaseViewCount();
        return ContentResponse.from(content);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ContentResponse> getListWithCursor(ContentCursorRequest request) {
        int size = request.size();

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1 이상 100 이하여야 합니다.");
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




    private void validateCheck(Content content, PrincipalDetails userDetails) {
        boolean isOwner = content.getAuthor().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("해당 콘텐츠에 대한 권한이 없습니다.");
        }
    }


}
