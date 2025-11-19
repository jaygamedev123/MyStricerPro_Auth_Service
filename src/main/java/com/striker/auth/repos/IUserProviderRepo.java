package com.striker.auth.repos;

import com.striker.auth.entity.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserProviderRepo extends JpaRepository<UserProvider, UUID> {

    Optional<UserProvider> findByAuthProviderAndProviderId(String authProvider, String providerId);
}
