package facoffe.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import facoffe.users.DTO.CreateUserDTO;
import facoffe.users.model.User;
import facoffe.users.service.UserService;
import jakarta.validation.Valid;
import facoffe.users.DTO.UserListResponseDTO;
import facoffe.users.DTO.UserResponseDTO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    //Criar usuário: POST /users
    @PostMapping("")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDTO userDTO) {
        // - Deve ser possível criar um novo usuário, fornecendo `name`, `email` e `roles`.
        
        User user = service.createUser(userDTO);


        return ResponseEntity
                .status(201)
                .body(user);
    }

    
    //Listar usuários: GET /users
    @GetMapping("")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserListResponseDTO> listUsers(

            //Filtro opcional de status
            @RequestParam(required = false)
            String status,

             //Filtro opcional de role
            @RequestParam(required = false)
            String role,

            //página atual
            @RequestParam(defaultValue = "0")
            Integer page,

            //quantidade de itens(users) por página
            @RequestParam(defaultValue = "20")
            Integer size
    ) {

        //Chama a camada de serviço no método de listar os usuários
        return ResponseEntity.ok(service.listUsers(status,role,page,size));
    }

    //Buscar usuário por ID: GET /users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable String userId,
            JwtAuthenticationToken auth) {

        return ResponseEntity.ok(
                service.getUserById(userId, auth));
    }

}
