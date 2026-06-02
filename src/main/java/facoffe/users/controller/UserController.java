package facoffe.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import facoffe.users.DTO.UserDTO;
import facoffe.users.model.User;
import facoffe.users.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("")
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        // - Deve ser possível criar um novo usuário, fornecendo `name`, `email` e `roles`.
        
        User user = service.createUser(userDTO);


        return ResponseEntity
                .status(201)
                .body(user);
    }
}
