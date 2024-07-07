package com.example.jwtTest.filter;

import com.example.jwtTest.service.MyUserDetailsService;
import com.example.jwtTest.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired // MyUserDetailService 주입
    private MyUserDetailsService myUserDetailsService;

    @Autowired // JwtUtil 주입
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // "Bearer " 이후 JWT 추출
            try {
                username = jwtUtil.extractUsername(jwt); // jwt에서 사용자 이름 추출하기
            } catch (ExpiredJwtException e) {
                // 토큰 만료되었을 때 처리
            }
        }

        // 사용자 이름이 존재하고, 현재 SecurityContext에 인증 정보 없는 경우
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 사용자 이름으로 UserDetails 가져옴
            UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(username);

            // jwt 유효한지 검증
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                // UsernamePasswordAuthenticationToken 생성하여 SecurityContext에 설정
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken); // 인증 정보 설정
            }
        }
        chain.doFilter(request, response); // 다음 필터 체인으로 요청 전달
    }
}
