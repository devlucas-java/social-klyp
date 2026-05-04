package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.auth.LoginDTO;
import com.github.devlucasjava.socialklyp.application.dto.request.auth.RegisterDTO;
import com.github.devlucasjava.socialklyp.application.dto.request.auth.UpdatePasswordDTO;
import com.github.devlucasjava.socialklyp.application.dto.request.auth.VerifyPasswordDTO;
import com.github.devlucasjava.socialklyp.application.dto.response.auth.JwtAuthDTO;
import com.github.devlucasjava.socialklyp.application.dto.response.utils.BooleanDTO;
import com.github.devlucasjava.socialklyp.application.service.AuthService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication and account management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = JwtAuthDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email or username already in use", content = @Content)
    })
    public ResponseEntity<JwtAuthDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(authService.register(registerDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates with username/email and password, returns JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtAuthDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    public ResponseEntity<JwtAuthDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using the current authenticated session")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed",
                    content = @Content(schema = @Schema(implementation = JwtAuthDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<JwtAuthDTO> refreshToken(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.refreshToken(user));
    }

    @PostMapping("/verify-password")
    @Operation(summary = "Verify password", description = "Checks if the provided password matches the authenticated user's password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns true if password matches",
                    content = @Content(schema = @Schema(implementation = BooleanDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<BooleanDTO> verifyPassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VerifyPasswordDTO dto) {
        return ResponseEntity.ok(authService.verifyPassword(user, dto));
    }

    @PutMapping("/password")
    @Operation(summary = "Update password", description = "Changes the authenticated user's password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or wrong current password", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePasswordDTO dto) {
        authService.updatePassword(user, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Confirms the user's email address using the token sent by email")
    @PreAuthorize("hasRole('USER')")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email verified successfully"),
            @ApiResponse(responseCode = "404", description = "Token not found or expired", content = @Content)
    })
    public ResponseEntity<Void> verifyEmail(@PathVariable("token") UUID token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-verification")
    @Operation(summary = "Send verification email", description = "Sends a new email verification link to the authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Verification email sent"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<Void> sendVerificationEmail(@AuthenticationPrincipal User user) {
        authService.sendVerificationEmail(user);
        return ResponseEntity.noContent().build();
    }
}
