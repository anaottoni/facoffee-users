package facoffe.users.DTO;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class CreateUserRequestDTO {
    @NotBlank(message = " Campo obrigatório ausente.")
    private String name;

    @NotBlank(message = " Campo obrigatório ausente.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotEmpty(message = " Campo obrigatório ausente.")
    private Set<String> roles = new HashSet<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
