package org.convenient.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProfilePicUpdateRequest {
    // Getters and setters
    private String email;
    private String base64Image; // base64-encoded image string

}
