package facoffe.users.config;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import facoffe.users.DTO.ErrorResponseDTO;
import facoffe.users.exception.EmailAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CASO 1: Captura erros de validação do contrato (Campos obrigatórios ausentes, e-mail inválido, etc.)
     * Disparado automaticamente pelo @Valid na Controller
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        // Junta todas as mensagens de campos que falharam (ex: "O campo 'name' é obrigatório | O e-mail deve ser válido")
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" | "));

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.SC_BAD_REQUEST,       // 400
                "Bad Request",
                errorMessage,                             // Mensagem detalhada dos campos
                request.getRequestURI()                   // Rota exata da requisição
        );

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, 
            HttpServletRequest request) {
        
        // Aqui você monta o JSON exatamente com os campos e a ordem que você quiser
        ErrorResponseDTO errorBody = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.SC_CONFLICT,
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.SC_CONFLICT).body(errorBody);
    }

    /**
     * CASO: Resposta '403' - Forbidden do contrato (MANAGER_OR_SELF falhou)
     */
    @ExceptionHandler(facoffe.users.exception.CustomAccessDeniedException.class) // MODIFICAÇÃO AQUI
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            facoffe.users.exception.CustomAccessDeniedException ex, // MODIFICAÇÃO AQUI
            HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                403,
                "Forbidden",
                ex.getMessage(), 
                request.getRequestURI()
        );

        return ResponseEntity.status(403).body(error);
    }

    /**
     * CASO: Resposta '404' - NotFound do contrato (ID informado não existe no Banco)
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            jakarta.persistence.EntityNotFoundException ex, 
            HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(404).body(error);
    }
}