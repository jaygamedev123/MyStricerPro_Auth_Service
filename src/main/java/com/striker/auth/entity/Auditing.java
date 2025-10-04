package com.striker.auth.entity;

import lombok.Data;

@Data
public class Auditing {
    private String createdBy;
    private String updatedBy;
}
