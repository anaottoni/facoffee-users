package facoffe.users.DTO;

import java.util.HashSet;
import java.util.Set;

public class UserDTO {
    private String name;
    private String email;
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
