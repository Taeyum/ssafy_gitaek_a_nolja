package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.BoardDto;
import com.ssafy.gitaek.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardMapper boardMapper;
    
    public List<BoardDto> getList() {
        return boardMapper.selectList();
    }
    
    
    @Transactional
    public BoardDto getDetail(int boardId, int currentUserId) {
        boardMapper.updateHit(boardId);
        BoardDto board = boardMapper.selectOne(boardId);
        
        // ★★★ 이 부분이 없으면 상세화면 들어갈 때 하트가 회색이 됩니다! ★★★
        if (board != null && currentUserId > 0) {
            boolean isWriter = (board.getUserId() == currentUserId);
            board.setMine(isWriter);
            
            // [체크] 좋아요 눌렀는지 확인해서 DTO에 담기
            int count = boardMapper.checkLike(boardId, currentUserId);
            board.setLiked(count > 0); 
        }
        return board;
    }

    
    @Transactional // [추가] 좋아요 토글 기능
    public boolean toggleLike(int boardId, int userId) {
        int check = boardMapper.checkLike(boardId, userId);
        
        if (check > 0) {
            // 이미 눌렀으면 -> 취소 (삭제 및 카운트 -1)
            boardMapper.deleteLike(boardId, userId);
            boardMapper.updateLikeCount(boardId, -1);
            return false; // 좋아요 취소됨
        } else {
            // 안 눌렀으면 -> 추가 (삽입 및 카운트 +1)
            boardMapper.insertLike(boardId, userId);
            boardMapper.updateLikeCount(boardId, 1);
            return true; // 좋아요 설정됨
        }
    }
    
    public void writeBoard(BoardDto board) {
        boardMapper.insertBoard(board);
    }
    
    public boolean updateBoard(BoardDto board, int userId) {
        BoardDto origin = boardMapper.selectOne(board.getBoardId());
        if (origin != null && origin.getUserId() == userId) {
            boardMapper.updateBoard(board);
            return true;
        }
        return false;
    }
    
    public boolean deleteBoard(int boardId, int userId) {
        BoardDto origin = boardMapper.selectOne(boardId);
        if (origin != null && origin.getUserId() == userId) {
            boardMapper.deleteBoard(boardId);
            return true;
        }
        return false;
    }
}