package com.github.devlucasjava.socialklyp.application.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data transfer object for verifying user password")
public class VerifyPasswordDTO {

    @Schema(example = "!12345678")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 30, message = "the password must be between 8 and 30 characters")
    public String password;
}
