package com.striker.auth.repos;

import com.striker.auth.entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ILoginSessionRepo extends JpaRepository<LoginSession, UUID> {
    LoginSession findByUserId(UUID userId);

    @Query("UPDATE LoginSession ls SET ls.isActive = false, ls.loggedOut = CURRENT_TIMESTAMP WHERE ls.userId = :userId AND ls.isActive = true")
    LoginSession invalidateUserSession(UUID userId);
}
