package com.minlish.controller;

import com.minlish.security.CustomUserPrincipal;
import com.minlish.security.JwtTokenProvider;
import com.minlish.dto.GoogleLoginRequest;
import com.minlish.dto.JwtResponse;
import com.minlish.dto.LoginRequest;
import com.minlish.dto.RegisterRequest;
import com.minlish.dto.ForgotPasswordRequest;
import com.minlish.entity.User;
import com.minlish.service.UserService;
import com.minlish.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 15:02
 * File      : AuthController
 */


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final EmailService emailService;
    private final RestTemplate restTemplate = new RestTemplate();
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại trong hệ thống");
        }
        User user = userOpt.get();
        // Sinh mật khẩu random
        String newPassword = generateRandomPassword(10);
        // Cập nhật mật khẩu mới (hash)
        userService.updatePassword(user, newPassword);
        // Gửi email
        emailService.sendEmail(user.getEmail(), "Mật khẩu mới Minlish", "Mật khẩu mới của bạn: " + newPassword);
        return ResponseEntity.ok("Mật khẩu mới đã được gửi về email của bạn");
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserPrincipal userPrincipal)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể lấy thông tin người dùng từ phiên xác thực");
        }

        return ResponseEntity.ok(new JwtResponse(
            jwt,
            userPrincipal.getId(),
            userPrincipal.getEmail(),
            userPrincipal.getFullName()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getLearningGoal(),
                request.getLevel()
        );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getEmail(), user.getFullName()));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        if ((request.getIdToken() == null || request.getIdToken().isBlank())
                && (request.getAccessToken() == null || request.getAccessToken().isBlank())) {
            return ResponseEntity.badRequest().body("Thiếu Google token");
        }

        Map<String, Object> tokenInfo = request.getIdToken() != null && !request.getIdToken().isBlank()
                ? verifyGoogleIdToken(request.getIdToken())
                : verifyGoogleAccessToken(request.getAccessToken());

        String email = stringValue(tokenInfo.get("email"));
        boolean emailVerified = Boolean.parseBoolean(stringValue(tokenInfo.get("email_verified")));
        if (email == null || email.isBlank() || !emailVerified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Google token không hợp lệ hoặc email chưa xác thực");
        }

        String fullName = stringValue(tokenInfo.get("name"));
        User user = userService.findOrCreateGoogleUser(email, fullName);

        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities().isEmpty()
                        ? List.of(new SimpleGrantedAuthority("USER"))
                        : userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt, user.getId(), user.getEmail(), user.getFullName()));
    }

    private Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(
                    "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}",
                    Map.class,
                    idToken);
            if (response == null || response.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Không nhận được dữ liệu xác thực từ Google");
            }

            String audience = stringValue(response.get("aud"));
            if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(audience)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Google token không dành cho ứng dụng này");
            }

            String issuer = stringValue(response.get("iss"));
            if (!"accounts.google.com".equals(issuer)
                    && !"https://accounts.google.com".equals(issuer)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Google token có issuer không hợp lệ");
            }
            return response;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Google token verification failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Xác thực Google thất bại");
        } catch (Exception ex) {
            log.error("Google token verification unexpected error", ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Dịch vụ xác thực Google tạm thời gián đoạn");
        }
    }

    private Map<String, Object> verifyGoogleAccessToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    requestEntity,
                    Map.class);

            Map<String, Object> response = responseEntity.getBody();
            if (response == null || response.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Không nhận được dữ liệu userinfo từ Google");
            }
            return response;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Google access token verification failed: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Xác thực Google thất bại");
        } catch (Exception ex) {
            log.error("Google access token verification unexpected error", ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Dịch vụ xác thực Google tạm thời gián đoạn");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
