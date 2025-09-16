package com.striker.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
public class Roles extends Auditing {
    @Id
    private UUID roleId;
    private String roleName;
    private String roleDescription;
    private boolean roleStatus;

}
