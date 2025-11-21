package com.ssafy.exam.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ssafy.exam.model.dto.Member;
import com.ssafy.exam.model.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String name, @RequestParam String password,
                        @RequestParam(required = false) String rememberMe,
                        HttpSession session, HttpServletResponse response) {
        
        if (rememberMe != null) {
            Cookie idCookie = new Cookie("id", name);
            idCookie.setMaxAge(60 * 60);
            idCookie.setPath("/");
            response.addCookie(idCookie);
        } else {
            Cookie idCookie = new Cookie("id", "");
            idCookie.setMaxAge(0);
            idCookie.setPath("/");
            response.addCookie(idCookie);
        }

        try {
            Member member = memberService.loginMember(name, password);
            if (member == null) {
                return "login"; // or redirect with an error message
            }
            session.setAttribute("loginUser", member);
            return "redirect:/";
        } catch (Exception e) {
            // Add error handling
            e.printStackTrace();
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/regist")
    public String registForm() {
        return "regist-member";
    }

    @PostMapping("/regist")
    public String regist(Member member) {
        try {
            memberService.registMember(member);
            return "redirect:/member/login";
        } catch (Exception e) {
            e.printStackTrace();
            return "regist-member";
        }
    }

    @GetMapping("/list")
    public String listMembers(Model model) {
        try {
            List<Member> members = memberService.allMember();
            model.addAttribute("members", members);
            return "member-list";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500"; // Or a more specific error page
        }
    }

    @GetMapping("/detail")
    public String memberDetail(@RequestParam String email, Model model) {
        try {
            Member member = memberService.getMember(email);
            model.addAttribute("member", member);
            return "member-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }

    @GetMapping("/modify")
    public String modifyForm(@RequestParam String email, Model model) {
        try {
            Member member = memberService.getMember(email);
            model.addAttribute("member", member);
            return "member-modify";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }

    @PostMapping("/modify")
    public String modify(Member member) {
        try {
            memberService.memberModify(member.getName(), member.getEmail(), member.getPassword());
            return "redirect:/member/list";
        } catch (Exception e) {
            e.printStackTrace();
            return "member-modify";
        }
    }

    @PostMapping("/remove")
    public String remove(@RequestParam String email) {
        try {
            memberService.memberRemove(email);
            return "redirect:/member/list";
        } catch (Exception e) {
            e.printStackTrace();
            // Maybe redirect with an error message
            return "redirect:/member/list";
        }
    }

    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "member-findpw";
    }

    @PostMapping("/find-password")
    public String findPassword(@RequestParam String email, @RequestParam String name, Model model) {
        try {
            String password = memberService.findPassword(email, name);
            model.addAttribute("password", password); // The JSP can show this if found
            return "member-findpw";
        } catch (Exception e) {
            e.printStackTrace();
            return "member-findpw";
        }
    }
}
