package com.minlish.config;

import com.minlish.entity.User;
import com.minlish.security.JwtTokenProvider;
import com.minlish.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.net.URLEncoder.encode;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${ui.url:https://minlish-websites.vercel.app}")
    private String uiUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = asString(oauth2User.getAttributes().get("email"));
        String fullName = asString(oauth2User.getAttributes().get("name"));

        if (email == null || email.isBlank()) {
            response.sendRedirect(uiUrl + "/auth#error=google_login_failed");
            return;
        }

        User user = userService.findOrCreateGoogleUser(email, fullName);
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        Authentication jwtAuth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        String jwt = jwtTokenProvider.generateToken(jwtAuth);

        String redirectUrl = UriComponentsBuilder
                .fromHttpUrl(uiUrl)
                .path("/auth")
                .fragment(buildAuthFragment(jwt, user.getId(), user.getEmail()))
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String buildAuthFragment(String accessToken, Long userId, String email) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("accessToken", accessToken);
        params.put("userId", userId == null ? "" : String.valueOf(userId));
        params.put("email", email == null ? "" : email);

        StringBuilder fragment = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!fragment.isEmpty()) {
                fragment.append('&');
            }
            fragment.append(encode(entry.getKey(), StandardCharsets.UTF_8));
            fragment.append('=');
            fragment.append(encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return fragment.toString();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
