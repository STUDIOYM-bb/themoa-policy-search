package com.themoa.policysearch.policy.bookmark;

import com.themoa.policysearch.common.response.ApiResponse;
import com.themoa.policysearch.policy.search.dto.PolicyResultItem;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public class PolicyBookmarkController {
    private final PolicyBookmarkService bookmarkService;

    public PolicyBookmarkController(PolicyBookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/api/policies/{policyId}/bookmarks")
    public ApiResponse<Void> add(@PathVariable Integer policyId) {
        bookmarkService.add(policyId);
        return ApiResponse.ok(null, "관심 정책으로 저장했습니다.");
    }

    @DeleteMapping("/api/policies/{policyId}/bookmarks")
    public ApiResponse<Void> remove(@PathVariable Integer policyId) {
        bookmarkService.remove(policyId);
        return ApiResponse.ok(null, "관심 정책에서 삭제했습니다.");
    }

    @GetMapping("/api/bookmarks")
    public ApiResponse<List<PolicyResultItem>> list() {
        return ApiResponse.ok(bookmarkService.list());
    }
}
