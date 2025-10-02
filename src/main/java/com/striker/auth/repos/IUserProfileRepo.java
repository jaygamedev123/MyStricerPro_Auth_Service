package com.striker.auth.repos;

import com.striker.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserProfileRepo extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByEmailOrMobile(String email, String mobile);

}
