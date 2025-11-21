package com.ssafy.exam.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.exam.model.dao.MemberDao;
import com.ssafy.exam.model.dto.Member;

@Service
public class MemberService {

    private final MemberDao memberDao;

    @Autowired
    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Transactional
    public int registMember(Member member) {
        return memberDao.registMember(member);
    }

    @Transactional(readOnly = true)
    public Member loginMember(String name, String password) {
        return memberDao.existMemberIdPassword(name, password);
    }

    @Transactional(readOnly = true)
    public List<Member> allMember() {
        return memberDao.selectAllMembers();
    }

    @Transactional(readOnly = true)
    public Member getMember(String email) {
        return memberDao.getMember(email);
    }

    @Transactional
    public int memberModify(String name, String email, String password) {
        Member member = new Member();
        member.setName(name);
        member.setEmail(email);
        member.setPassword(password);
        return memberDao.modifyMember(member);
    }

    @Transactional
    public int memberRemove(String email) {
        return memberDao.removeMember(email);
    }

    @Transactional(readOnly = true)
    public String findPassword(String email, String name) {
        return memberDao.findPassword(email, name);
    }
}
