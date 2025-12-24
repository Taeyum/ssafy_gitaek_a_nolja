package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.BoardDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardMapper {
    List<BoardDto> selectList();
    BoardDto selectOne(int boardId);
    void insertBoard(BoardDto board);
    void updateBoard(BoardDto board);
    void deleteBoard(int boardId);
    void updateHit(int boardId);
    
    // 기존 코드 아래에 추가
    int checkLike(int boardId, int userId); // 좋아요 여부 확인 (1이면 true, 0이면 false)
    void insertLike(int boardId, int userId); // 좋아요 추가
    void deleteLike(int boardId, int userId); // 좋아요 취소
    void updateLikeCount(int boardId, int count); // 게시글 테이블 like_count 변경 (+1 or -1)
}