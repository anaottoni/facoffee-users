package facoffe.users.DTO;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRequestDTO {

    @NotBlank(message = "Campo obrigatório ausente.")
    private String name;

    public UpdateUserRequestDTO() {
    }

    public UpdateUserRequestDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}