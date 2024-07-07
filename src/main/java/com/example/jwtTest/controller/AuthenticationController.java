package com.example.jwtTest.controller;

import com.example.jwtTest.model.User;
import com.example.jwtTest.repository.UserRepository;
import com.example.jwtTest.service.MyUserDetailsService;
import com.example.jwtTest.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController // RESTful 웹 서비스의 컨트롤러로 지정
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<String> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            // 사용자 인증
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );

            // 사용자 이름으로 UserDetails 로드
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

            // jwt 생성 후 반환
            String token = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(token);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authentication failed");
        }
    }


    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {
        // 사용자 이름이 이미 존재하는지 확인
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "This ID already exists";
        }
        // 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully"; // 성공 메시지 반환
    }

    @GetMapping("/test")
    public ResponseEntity<String> testApi() {
        return ResponseEntity.ok("JWT is valid and you are authorized to access this endpoint.");
    }
}
