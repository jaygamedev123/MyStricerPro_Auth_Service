package com.striker.auth.repos;

import com.striker.auth.entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ILoginSessionRepo extends JpaRepository<LoginSession, UUID> {

    LoginSession findByUserId(UUID userId);

    List<LoginSession> findByIsActiveTrue();
}
