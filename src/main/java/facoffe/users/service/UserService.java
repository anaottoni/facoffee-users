package facoffe.users.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import facoffe.users.DTO.CreateUserDTO;
import facoffe.users.exception.EmailAlreadyExistsException;
import facoffe.users.model.Role;
import facoffe.users.model.User;
import facoffe.users.repository.RoleRepository;
import facoffe.users.repository.UserRepository;
import jakarta.ws.rs.core.Response;

@Service
public class UserService {
    @Autowired
    private Keycloak keycloak;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${realm}")
    private String realm;

    public User createUser(CreateUserDTO userDTO){
        if (userRepository.existsByEmail(userDTO.getEmail())){
            throw new EmailAlreadyExistsException("E-mail já existe no sistema.");
        }

        // criando user local
        User newUser = new User();
        newUser.setName(userDTO.getName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setStatus("ACTIVE");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        if(userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()){
            for(String roleName : userDTO.getRoles()){
                Role role = roleRepository.findByName(roleName);
                newUser.getRoles().add(role);
            }
        }
        // else{
        //     newUser.getRoles().add(roleRepository.findByName("PARTICIPANT"));
        // }

        // criando user no keycloak
        UserRepresentation kUser = new UserRepresentation();
        kUser.setUsername(userDTO.getEmail());
        kUser.setFirstName(userDTO.getName());
        kUser.setEmail(userDTO.getEmail());
        kUser.setEnabled(true);

        kUser.setRequiredActions(List.of("VERIFY_EMAIL","UPDATE_PASSWORD"));

        // salvando user no keycloak 
        Response response = keycloak.realm(realm).users().create(kUser);
    
        // verifica resposta e pega id para salvar no BD
        if (response.getStatus() == 201) {
            String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
            
            newUser.setId(keycloakUserId); 

            // atribuindo roles no keycloak
            List<RoleRepresentation> rolesToAssign = new ArrayList<>();
            
            for (String roleName : userDTO.getRoles()) {
                
                RoleRepresentation kRole = keycloak.realm(realm).roles().get(roleName).toRepresentation();
                rolesToAssign.add(kRole);
            }

            keycloak.realm(realm).users().get(keycloakUserId).roles().realmLevel().add(rolesToAssign);

            // dispara  o e-mail para que o usuario defina a senha
            keycloak.realm(realm).users().get(keycloakUserId)
                    .executeActionsEmail(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"));
        } else {
            throw new RuntimeException("Falha ao criar usuário no Keycloak. Status: " + response.getStatus());
        }

        // salvando user no bd
        userRepository.save(newUser);

        return newUser;
    }
}
