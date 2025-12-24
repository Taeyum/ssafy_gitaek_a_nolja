package com.ssafy.gitaek.controller;

import com.ssafy.gitaek.dto.BoardDto;
import com.ssafy.gitaek.jwt.CustomUserDetails;
import com.ssafy.gitaek.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @GetMapping
    public ResponseEntity<List<BoardDto>> list() {
        return ResponseEntity.ok(boardService.getList());
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> detail(@PathVariable int boardId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        int userId = (userDetails != null) ? userDetails.getUser().getUserId() : 0;
        return ResponseEntity.ok(boardService.getDetail(boardId, userId));
    }

    @PostMapping
    public ResponseEntity<?> write(@RequestBody BoardDto dto,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        dto.setUserId(userDetails.getUser().getUserId());
        boardService.writeBoard(dto);
        return ResponseEntity.ok("작성 완료");
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<?> update(@PathVariable int boardId,
                                    @RequestBody BoardDto dto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        dto.setBoardId(boardId);
        boolean result = boardService.updateBoard(dto, userDetails.getUser().getUserId());
        return result ? ResponseEntity.ok("수정 완료") : ResponseEntity.status(403).body("권한 없음");
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> delete(@PathVariable int boardId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        boolean result = boardService.deleteBoard(boardId, userDetails.getUser().getUserId());
        return result ? ResponseEntity.ok("삭제 완료") : ResponseEntity.status(403).body("권한 없음");
    }
    
    @PostMapping("/{boardId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable int boardId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        
        boolean isLiked = boardService.toggleLike(boardId, userDetails.getUser().getUserId());
        // 현재 상태(true=좋아요, false=취소)를 반환
        return ResponseEntity.ok(isLiked);
    }
}