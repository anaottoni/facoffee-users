package facoffe.users.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import facoffe.users.DTO.CreateUserRequestDTO;
import facoffe.users.DTO.DeleteUserRequestDTO;
import facoffe.users.DTO.UpdateUserRequestDTO;
import facoffe.users.event.EventEnvelope;
import facoffe.users.event.UserDeactivatedPayload;
import facoffe.users.exception.CustomAccessDeniedException;
import facoffe.users.exception.EmailAlreadyExistsException;
import facoffe.users.exception.UserNotFoundException;
import facoffe.users.model.Role;
import facoffe.users.model.User;
import facoffe.users.repository.RoleRepository;
import facoffe.users.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import facoffe.users.DTO.PageDTO;
import facoffe.users.DTO.UserListResponseDTO;
import facoffe.users.DTO.UserResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;

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

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public User createUser(CreateUserRequestDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("E-mail já existe no sistema.");
        }

        // criando user local
        User newUser = new User();
        newUser.setName(userDTO.getName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setStatus("ACTIVE");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName);
                newUser.getRoles().add(role);
            }
        } else {
            newUser.getRoles().add(roleRepository.findByName("PARTICIPANT"));
        }

        // criando user no keycloak
        UserRepresentation kUser = new UserRepresentation();
        kUser.setUsername(userDTO.getEmail());
        kUser.setFirstName(userDTO.getName());
        kUser.setEmail(userDTO.getEmail());
        kUser.setEnabled(true);

        kUser.setRequiredActions(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"));

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

            // dispara o e-mail para que o usuario defina a senha
            keycloak.realm(realm).users().get(keycloakUserId)
                    .executeActionsEmail(List.of("VERIFY_EMAIL", "UPDATE_PASSWORD"));
        } else {
            throw new RuntimeException("Falha ao criar usuário no Keycloak. Status: " + response.getStatus());
        }

        // salvando user no bd
        userRepository.save(newUser);

        return newUser;
    }


    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(user);
    }

    public UserListResponseDTO listUsers(String status,String role,Integer page,Integer size) {
        
        //Cria objeto de paginação
        //page = página desejada
        //size = quantidade de registros
        Pageable pageable = PageRequest.of(page, size);
        
        //Variável que receberá os usuários encontrados
        Page<User> users;

        //Se vier status e role
        if (status != null && role != null) {

            users = userRepository.findByStatusAndRoles_Name(status,role,pageable);

        //Se vier apenas status
        } else if (status != null) {

            users = userRepository.findByStatus(status,pageable);

        //Se vier apenas role
        } else if (role != null) {

            users = userRepository.findByRoles_Name(role,pageable);

        //Sem filtros
        } else {

            users = userRepository.findAll(pageable);
        }

        //Monta resposta de acordo com o Swagger
        return new UserListResponseDTO(

                //Converte cada User em UserResponseDTO
                users.getContent()
                        .stream()
                        //para cada user converte em userDTO
                        .map(this::toResponseDTO)
                        .toList(),

                //Informações da paginação
                new PageDTO(
                        users.getNumber(),
                        users.getSize(),
                        users.getTotalElements(),
                        users.getTotalPages()
                )
        );
    
    }

    public UserResponseDTO getUserById(String userId, JwtAuthenticationToken auth) {

        String authenticatedUserId = auth.getToken().getSubject();

        boolean isManager = auth.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (!isManager && !authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException(
                    "Usuário autenticado não possui permissão para acessar este recurso.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("Campo obrigatório ausente."));

        return toResponseDTO(user);
    }


    public User updateUser(String idToDelete, UpdateUserRequestDTO updateDTO, Authentication authentication) {

        
        if (authentication == null) {
        throw new facoffe.users.exception.CustomAccessDeniedException(
                "Usuário não está autenticado no contexto de segurança.");
    }

    String loggedUserEmail = null;

    // pega o email do token
    if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
        loggedUserEmail = jwt.getClaimAsString("email");
    }

        // verifica se o usuario e manager
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));

        boolean isSelf = false;

        // verificando se o token do usuario e o mesmo que ele deseja alterar
        if (loggedUserEmail != null) {
            User userLocal = userRepository.findByEmail(loggedUserEmail);

            if (userLocal != null && userLocal.getId() != null) {
                isSelf = userLocal.getId().equals(idToDelete);
            }
        }

        // se nao gor manager ou nao estiver tentando modificar a si proprio, dispara a
        // exception
        if (!isManager && !isSelf) {
            throw new facoffe.users.exception.CustomAccessDeniedException(
                    "Usuário autenticado não possui permissão para acessar este recurso.");
        }

        // busca id que quer editar no banco
        User user = userRepository.findById(idToDelete)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Recurso não encontrado."));

        user.setName(updateDTO.getName());
        user.setUpdatedAt(LocalDateTime.now());

        // atualiza dados no keycloak
        try {
            UserRepresentation kUser = keycloak.realm(realm).users().get(idToDelete).toRepresentation();
            kUser.setFirstName(updateDTO.getName());

            keycloak.realm(realm).users().get(idToDelete).update(kUser);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao atualizar dados básicos no Keycloak: " + e.getMessage());
        }

        // atualiza dados no banco
        return userRepository.save(user);
    }

    public User deleteUser(String idToDelete, DeleteUserRequestDTO deleteDTO, Authentication authentication) {
        String loggedUserEmail = null;

        // pega o email do token
        if (authentication != null
                && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            loggedUserEmail = jwt.getClaimAsString("email");
        }

        // verifica se o usuario e manager
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));

        boolean isSelf = false;

        // verificando se o token do usuario e o mesmo que ele deseja alterar
        if (loggedUserEmail != null) {
            User userLocal = userRepository.findByEmail(loggedUserEmail);

            if (userLocal != null && userLocal.getId() != null) {
                isSelf = userLocal.getId().equals(idToDelete);
            }
        }

        // se nao gor manager ou nao estiver tentando modificar a si proprio, dispara a
        // exception
        if (!isManager && !isSelf) {
            throw new facoffe.users.exception.CustomAccessDeniedException(
                    "Usuário autenticado não possui permissão para acessar este recurso.");
        }

        // busca id que quer editar no banco
        User user = userRepository.findById(idToDelete)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Recurso não encontrado."));

        user.setStatus("INACTIVE");
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeactivatedAt(LocalDateTime.now());

        // atualiza dados no keycloak
        try {
            UserRepresentation kUser = keycloak.realm(realm).users().get(idToDelete).toRepresentation();
            kUser.setEnabled(false);

            keycloak.realm(realm).users().get(idToDelete).update(kUser);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao desativar usuário no Keycloak: " + e.getMessage());
        }

        // salvando no banco
        User savedUser = userRepository.save(user);

        // publicando evento
        try {
            UserDeactivatedPayload payload = new UserDeactivatedPayload(idToDelete, deleteDTO.getReason());
            EventEnvelope<UserDeactivatedPayload> event = new EventEnvelope<>("UserDeactivated", payload);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

            String jsonEvent = mapper.writeValueAsString(event);

            // NOME DA FILA/ROUTING KEY ALTERADO AQUI:
            rabbitTemplate.convertAndSend("participation.user-deactivated", jsonEvent);
            System.out.println("[RabbitMQ] Evento UserDeactivated publicado com sucesso para o ID: " + idToDelete);

        } catch (Exception e) {
            System.err.println("[RabbitMQ] Erro ao publicar evento: " + e.getMessage());
        }

        return savedUser;
    }

    public User replaceUserRoles(String userId, List<String> newRoleNames, Authentication authentication) {
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));

        if (!isManager) {
            throw new CustomAccessDeniedException(
                    "Apenas gestores podem alterar papéis de usuários.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Usuário não encontrado."));

        try {
            var userResource = keycloak.realm(realm).users().get(userId);

            List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll();
            if (!currentRoles.isEmpty()) {
                userResource.roles().realmLevel().remove(currentRoles);
            }

            List<RoleRepresentation> rolesToAddKeycloak = new ArrayList<>();
            Set<Role> rolesToAddLocal = new HashSet<>(); 

            for (String roleName : newRoleNames) {
                RoleRepresentation kRole = keycloak.realm(realm).roles().get(roleName).toRepresentation();
                rolesToAddKeycloak.add(kRole);

                Role roleEntity = roleRepository.findByName(roleName);
                if (roleEntity != null) {
                    rolesToAddLocal.add(roleEntity);
                } else {
                    throw new RuntimeException("Role '" + roleName + "' não encontrada no sistema.");
                }
            }

            userResource.roles().realmLevel().add(rolesToAddKeycloak);

            user.getRoles().clear(); 
            user.getRoles().addAll(rolesToAddLocal); 
            user.setUpdatedAt(LocalDateTime.now());

            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao substituir roles: " + e.getMessage());
        }
    }
}