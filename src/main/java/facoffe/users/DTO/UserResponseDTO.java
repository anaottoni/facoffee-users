package facoffe.users.DTO;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import facoffe.users.model.User;
import facoffe.users.model.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({ "createdAt", "deactivatedAt", "email", "id", "name", "roles", "status", "updatedAt" })
public class UserResponseDTO {
    
    private LocalDateTime createdAt;
    private LocalDateTime deactivatedAt;
    private String email;
    private String id;
    private String name;
    private List<String> roles; // Convertido para lista de Strings (nomes das roles)
    private String status;
    private LocalDateTime updatedAt;

    // Construtor padrão (Necessário para o Jackson)
    public UserResponseDTO() {
    }

    // Construtor Inteligente: Recebe a Entidade User e faz o mapeamento
    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.deactivatedAt = user.getDeactivatedAt(); // Caso sua entidade possua esse campo
        
        // Mapeia a lista de objetos 'Role' do banco para uma lista limpa de Strings
        if (user.getRoles() != null) {
            this.roles = user.getRoles().stream()
                    .filter(Objects::nonNull)
                    .map(Role::getName) // Pega apenas a String com o nome da role (ex: "ADMIN")
                    .toList();
        }
    }

    // --- GETTERS E SETTERS ---

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(LocalDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}