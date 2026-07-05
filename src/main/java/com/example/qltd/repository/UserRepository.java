package com.example.qltd.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.qltd.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
