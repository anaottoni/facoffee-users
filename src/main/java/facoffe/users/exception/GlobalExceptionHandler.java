package facoffe.users.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import facoffe.users.DTO.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Erros de validação (@Valid na Controller - Status 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" | "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", errorMessage, request.getRequestURI()
        ));
    }

    // 2. E-mail já cadastrado (Status 409)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI()
        ));
    }

    // 3. Usuário não encontrado na Service (Status 404)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI()
        ));
    }

    // 4. Erro 403 da Service (MANAGER_OR_SELF falhou na regra de negócio)
    @ExceptionHandler(facoffe.users.exception.CustomAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomAccessDenied(
            facoffe.users.exception.CustomAccessDeniedException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), request.getRequestURI()
        ));
    }

    // 5. Erro 404 do Banco (EntityNotFoundException)
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            jakarta.persistence.EntityNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI()
        ));
    }

    // 6. Erro 401 - Token ausente ou inválido (Vindo do Filtro)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Unauthorized", "Autenticação necessária para acessar este recurso.", request.getRequestURI()
        ));
    }

    // 7. Erro 403 - Bloqueio por anotação (Ex: @PreAuthorize no listUsers)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleSpringAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), "Forbidden", "Você não tem permissão para acessar este recurso.", request.getRequestURI()
                ));
    }

    /**
     * CAPTURA GLOBAL: Impede que qualquer erro desconhecido seja redirecionado para o /api/error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Isso vai cuspir na tela a classe exata do erro e a mensagem real do problema
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                "Internal Server Error",
                "Erro Oculto Revelado -> " + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}