package com.example.qltd.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.qltd.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
