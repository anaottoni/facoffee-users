package facoffe.users.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import facoffe.users.model.Role;
import facoffe.users.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createRoleIfNotExists(1L, "PARTICIPANT");
        createRoleIfNotExists(2L, "MANAGER");
    }

    private void createRoleIfNotExists(Long id, String name) {
        Role role = roleRepository.findByName(name);

        if (role == null) {
            roleRepository.save(new Role(id, name));
        }
    }
}