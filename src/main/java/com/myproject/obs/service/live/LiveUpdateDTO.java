package com.myproject.obs.service.live;

import jakarta.validation.constraints.NotBlank;

public record LiveUpdateDTO(
        String title,
        String description,
        String publicUrl
) {
}
