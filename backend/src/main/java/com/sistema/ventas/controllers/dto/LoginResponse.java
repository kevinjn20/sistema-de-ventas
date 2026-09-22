package com.sistema.ventas.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private String username;
    private String rol;
    private Long userId;

    public static LoginResponse bearer(String token, String username, String rol, Long userId) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(username)
                .rol(rol)
                .userId(userId)
                .build();
    }
}
