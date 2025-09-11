package org.convenient.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String email;
    private String fullName;
    private String phone;
}
