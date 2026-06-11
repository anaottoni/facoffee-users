package facoffe.users.DTO;

public record PageDTO(
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
