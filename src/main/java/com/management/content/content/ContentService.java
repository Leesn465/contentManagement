package com.management.content.content;

import com.management.content.auth.PrincipalDetails;
import com.management.content.auth.PrincipalUserDetailsService;
import com.management.content.common.exception.ForbiddenException;
import com.management.content.common.exception.ResourceNotFoundException;
import com.management.content.content.DTO.ContentCreateRequest;
import com.management.content.content.DTO.ContentResponse;
import com.management.content.content.DTO.ContentUpdateRequest;
import com.management.content.content.DTO.PageResponse;
import com.management.content.user.Role;
import com.management.content.user.User;
import com.management.content.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PageResponse<ContentResponse> getList(int page, int size) {

        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1 이상 100 이하여야 합니다.");
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<Content> contentPage =
                contentRepository.findAllByOrderByCreatedDateDesc(pageable);

        Page<ContentResponse> mapped =
                contentPage.map(ContentResponse::from);

        return PageResponse.from(mapped);
    }



    private void validateCheck(Content content, PrincipalDetails userDetails) {
        boolean isOwner = content.getAuthor().getId().equals(userDetails.getId());
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("해당 콘텐츠에 대한 권한이 없습니다.");
        }
    }


}
