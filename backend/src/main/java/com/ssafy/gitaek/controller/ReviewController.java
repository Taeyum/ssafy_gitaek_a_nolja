package com.ssafy.gitaek.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.gitaek.dto.ReviewDto;
import com.ssafy.gitaek.jwt.CustomUserDetails;
import com.ssafy.gitaek.service.ReviewService;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	// 목록 조회
	@GetMapping
	public ResponseEntity<List<ReviewDto>> list(@AuthenticationPrincipal CustomUserDetails userDetails) {
		int userId = (userDetails != null) ? userDetails.getUser().getUserId() : 0;
		return ResponseEntity.ok(reviewService.getList(userId));
	}

	// [추가] 내가 좋아요한 리뷰 목록 조회
	// GET /api/review/liked
	@GetMapping("/liked")
	public ResponseEntity<List<ReviewDto>> likedList(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.status(401).build();
		return ResponseEntity.ok(reviewService.getLikedList(userDetails.getUser().getUserId()));
	}

	// 상세 조회
	@GetMapping("/{reviewId}")
	public ResponseEntity<ReviewDto> detail(@PathVariable int reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		int userId = (userDetails != null) ? userDetails.getUser().getUserId() : 0;
		return ResponseEntity.ok(reviewService.getDetail(reviewId, userId));
	}

	// 작성
	@PostMapping
	public ResponseEntity<?> write(@RequestBody ReviewDto dto, @AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.status(401).build();
		dto.setUserId(userDetails.getUser().getUserId());
		reviewService.writeReview(dto);
		return ResponseEntity.ok("리뷰 작성 완료");
	}

	// 수정
	@PutMapping("/{reviewId}")
	public ResponseEntity<?> update(@PathVariable int reviewId, @RequestBody ReviewDto dto,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.status(401).build();
		dto.setReviewId(reviewId);
		boolean result = reviewService.updateReview(dto, userDetails.getUser().getUserId());
		return result ? ResponseEntity.ok("수정 완료") : ResponseEntity.status(403).body("권한 없음");
	}

	// 삭제
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<?> delete(@PathVariable int reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.status(401).build();
		boolean result = reviewService.deleteReview(reviewId, userDetails.getUser().getUserId());
		return result ? ResponseEntity.ok("삭제 완료") : ResponseEntity.status(403).body("권한 없음");
	}

	// 좋아요
	@PostMapping("/{reviewId}/like")
	public ResponseEntity<?> toggleLike(@PathVariable int reviewId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.status(401).build();

		boolean isLiked = reviewService.toggleLike(reviewId, userDetails.getUser().getUserId());
		return ResponseEntity.ok(isLiked);
	}
}