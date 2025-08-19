package com.myproject.obs.service.live;


import jakarta.validation.constraints.NotBlank;

public record LiveDTO(
        @NotBlank(message = "O título é obrigatório")
        String title,

        String description, // opcional

        @NotBlank(message = "A streamKey é obrigatória")
        String streamKey,

        @NotBlank(message = "A publicUrl é obrigatória")
        String publicUrl
) {
}

