package com.minlish.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {

    private String idToken;

    private String accessToken;
}
