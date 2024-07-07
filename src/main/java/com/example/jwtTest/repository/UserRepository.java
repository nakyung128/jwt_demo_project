package com.example.jwtTest.repository;

import com.example.jwtTest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 사용자 이름으로 사용자 찾기
    Optional<User> findByUsername(String username);
}