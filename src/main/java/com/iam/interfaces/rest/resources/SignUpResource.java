package com.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SignUpResource(@NotBlank String username, @NotBlank String password, List<String> roles) {
}
