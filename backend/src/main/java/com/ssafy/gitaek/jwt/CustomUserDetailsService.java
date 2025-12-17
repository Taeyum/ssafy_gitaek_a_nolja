package com.ssafy.gitaek.jwt;

import com.ssafy.gitaek.mapper.UserMapper;
import com.ssafy.gitaek.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.selectUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("해당 이메일을 가진 사용자가 없습니다: " + email);
        }
        return new CustomUserDetails(user);
    }
}