package com.minlish.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:46
 * File      : JwtResponse
 */

@Data
@AllArgsConstructor
public class JwtResponse {
    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("tokenType")
    private String tokenType = "Bearer";

    @JsonProperty("userId")
    private Long userId;

    private String email;
    private String fullName;

    public JwtResponse(String accessToken, Long userId, String email, String fullName) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    @JsonProperty("token")
    public String getToken() {
        return accessToken;
    }
}