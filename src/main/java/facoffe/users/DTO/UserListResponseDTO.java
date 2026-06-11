package facoffe.users.DTO;

import java.util.List;

public record UserListResponseDTO(
        List<UserResponseDTO> items,
        PageDTO page
) {}