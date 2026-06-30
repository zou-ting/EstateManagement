package com.autogen.propmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String username;
    private String name;
    private String role;
    private String token;
    private Long ownerId;
    private String avatar;
}
