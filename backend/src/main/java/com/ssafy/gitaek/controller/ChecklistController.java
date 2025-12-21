package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.jwt.CustomUserDetails;
import com.ssafy.gitaek.service.ChecklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checklist")
public class ChecklistController {

	@Autowired
	private ChecklistService checklistService;

	// 1. 조회: /api/checklist?planId=1 (planId 없으면 0)
	@GetMapping
	public ResponseEntity<?> getList(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(defaultValue = "0") int planId) {

		if (userDetails == null)
			return ResponseEntity.status(401).build();

		// 해당 유저의 + 해당 여행(planId)의 리스트만 가져옴
		return ResponseEntity.ok(checklistService.getMyList(userDetails.getUser().getUserId(), planId));
	}

	// 2. 추가 (내용 + planId 받기)
	@PostMapping
	public ResponseEntity<?> addItem(@RequestBody Map<String, Object> body,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		if (userDetails == null)
			return ResponseEntity.status(401).build();

		String content = (String) body.get("content");

		// ★ [수정] 숫자가 문자로 오든, Long으로 오든 안전하게 int로 변환하는 코드
		int planId = 0;
		if (body.containsKey("planId") && body.get("planId") != null) {
			planId = Integer.parseInt(String.valueOf(body.get("planId")));
		}

		checklistService.addItem(userDetails.getUser().getUserId(), planId, content);
		return ResponseEntity.ok("추가됨");
	}

	// 3. 개별 삭제 (기존 기능)
	@DeleteMapping("/{checkId}")
	public ResponseEntity<?> deleteItem(@PathVariable int checkId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		if (userDetails == null)
			return ResponseEntity.status(401).build();

		checklistService.deleteItem(checkId, userDetails.getUser().getUserId());
		return ResponseEntity.ok("삭제됨");
	}

	// 4. 개별 체크 토글 (기존 기능)
	@PutMapping("/{checkId}")
	public ResponseEntity<?> toggleItem(@PathVariable int checkId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		if (userDetails == null)
			return ResponseEntity.status(401).build();

		checklistService.toggleItem(checkId, userDetails.getUser().getUserId());
		return ResponseEntity.ok("상태 변경됨");
	}

	// 5. 전체 삭제 (해당 여행 planId에 포함된 것만)
	@DeleteMapping("/all")
	public ResponseEntity<?> deleteAll(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(defaultValue = "0") int planId) {

		if (userDetails == null)
			return ResponseEntity.status(401).build();

		checklistService.deleteAll(userDetails.getUser().getUserId(), planId);
		return ResponseEntity.ok("전체 삭제 완료");
	}

	// 6. 전체 완료 (해당 여행 planId에 포함된 것만)
	@PutMapping("/all")
	public ResponseEntity<?> checkAll(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(defaultValue = "0") int planId) {

		if (userDetails == null)
			return ResponseEntity.status(401).build();

		checklistService.checkAll(userDetails.getUser().getUserId(), planId);
		return ResponseEntity.ok("전체 완료 처리");
	}

	@GetMapping("/plans")
	public ResponseEntity<?> getMyPlans(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null)
			return ResponseEntity.status(401).build();

		// 서비스에서 여행 목록 가져오기 (기존 TripService 활용하거나 Mapper 호출)
		// 여기서는 편의상 ChecklistService를 통해 Mapper를 부른다고 가정합니다.
		return ResponseEntity.ok(checklistService.getMyPlans(userDetails.getUser().getUserId()));
	}

}