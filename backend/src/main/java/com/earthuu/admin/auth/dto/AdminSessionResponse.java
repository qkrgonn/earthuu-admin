package com.earthuu.admin.auth.dto;

import java.util.UUID;

public record AdminSessionResponse(UUID id, String email, String role) {
}
