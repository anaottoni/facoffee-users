package facoffe.users.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDTO(
        String id,
        String name,
        String email,
        String status,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deactivatedAt
) {}