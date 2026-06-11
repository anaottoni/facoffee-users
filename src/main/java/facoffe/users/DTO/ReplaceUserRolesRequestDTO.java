package facoffe.users.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReplaceUserRolesRequestDTO {

    @NotNull(message = "A lista de papéis não pode ser nula")
    @NotEmpty(message = "A lista de papéis não pode estar vazia")
    private List<String> roles;

    public ReplaceUserRolesRequestDTO() {
    }

    public ReplaceUserRolesRequestDTO(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}