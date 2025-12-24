package com.ssafy.gitaek.service;

import com.ssafy.gitaek.dto.ReviewDto;
import com.ssafy.gitaek.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;
    
    // [수정됨] userId를 받아서 매퍼로 전달
    public List<ReviewDto> getList(int userId) {
        return reviewMapper.selectList(userId);
    }
    
    // [추가] 좋아요한 리뷰 목록 가져오기
    public List<ReviewDto> getLikedList(int userId) {
        return reviewMapper.selectLikedList(userId);
    }
    
    @Transactional
    public ReviewDto getDetail(int reviewId, int currentUserId) {
        reviewMapper.updateHit(reviewId);
        ReviewDto review = reviewMapper.selectOne(reviewId);
        
        if (review != null && currentUserId > 0) {
            boolean isWriter = (review.getUserId() == currentUserId);
            review.setMine(isWriter);
            
            int count = reviewMapper.checkLike(reviewId, currentUserId);
            review.setLiked(count > 0); 
        }
        return review;
    }

    @Transactional
    public boolean toggleLike(int reviewId, int userId) {
        int check = reviewMapper.checkLike(reviewId, userId);
        
        if (check > 0) {
            reviewMapper.deleteLike(reviewId, userId);
            reviewMapper.updateLikeCount(reviewId, -1);
            return false; 
        } else {
            reviewMapper.insertLike(reviewId, userId);
            reviewMapper.updateLikeCount(reviewId, 1);
            return true; 
        }
    }
    
    public void writeReview(ReviewDto review) {
        reviewMapper.insertReview(review);
    }
    
    public boolean updateReview(ReviewDto review, int userId) {
        ReviewDto origin = reviewMapper.selectOne(review.getReviewId());
        if (origin != null && origin.getUserId() == userId) {
            reviewMapper.updateReview(review);
            return true;
        }
        return false;
    }
    
    public boolean deleteReview(int reviewId, int userId) {
        ReviewDto origin = reviewMapper.selectOne(reviewId);
        if (origin != null && origin.getUserId() == userId) {
            reviewMapper.deleteReview(reviewId);
            return true;
        }
        return false;
    }
}