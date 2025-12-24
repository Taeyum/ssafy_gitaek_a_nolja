package com.ssafy.gitaek.mapper;

import com.ssafy.gitaek.dto.ReviewDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ReviewMapper {
    // [수정됨] 파라미터 int userId 추가
    List<ReviewDto> selectList(int userId);
    
    List<ReviewDto> selectLikedList(int userId);
    
    ReviewDto selectOne(int reviewId);
    void insertReview(ReviewDto review);
    void updateReview(ReviewDto review);
    void deleteReview(int reviewId);
    void updateHit(int reviewId);
    
    // 좋아요 관련
    int checkLike(int reviewId, int userId); 
    void insertLike(int reviewId, int userId); 
    void deleteLike(int reviewId, int userId); 
    void updateLikeCount(int reviewId, int count); 
}