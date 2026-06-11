package facoffe.users.DTO;

import jakarta.validation.constraints.NotBlank;

public class DeleteUserRequestDTO {
    @NotBlank(message = "O motivo da desativação é obrigatório.")
    private String reason;

    public DeleteUserRequestDTO() {
    }

    public DeleteUserRequestDTO(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
