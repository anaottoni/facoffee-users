package facoffe.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import facoffe.users.DTO.CreateUserRequestDTO;
import facoffe.users.DTO.DeleteUserRequestDTO;
import facoffe.users.DTO.ReplaceUserRolesRequestDTO;
import facoffe.users.DTO.UpdateUserRequestDTO;
import facoffe.users.DTO.UserResponseDTO;
import facoffe.users.model.User;
import facoffe.users.service.UserService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO userDTO) {

        User user = service.createUser(userDTO);

        UserResponseDTO userResponse = new UserResponseDTO(user);

        return ResponseEntity
                .status(201)
                .body(userResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable String id,
            @Valid @RequestBody UpdateUserRequestDTO updateDTO, Authentication authentication) {

        User updatedUser = service.updateUser(id, updateDTO, authentication);

        return ResponseEntity.ok(new UserResponseDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponseDTO> DeleteUser(@PathVariable String id,
            @Valid @RequestBody DeleteUserRequestDTO deleteDTO, Authentication authentication) {

        User updatedUser = service.deleteUser(id, deleteDTO, authentication);

        return ResponseEntity.ok(new UserResponseDTO(updatedUser));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserResponseDTO> replaceUserRoles(
            @PathVariable String userId,
            @Valid @RequestBody ReplaceUserRolesRequestDTO requestDTO,
            Authentication authentication) {

        User updatedUser = service.replaceUserRoles(userId, requestDTO.getRoles(), authentication);

        return ResponseEntity.ok(new UserResponseDTO(updatedUser));
    }
}
