package br.edu.ifsertaope.assistencia.exception;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import br.edu.ifsertaope.assistencia.exception.ErroResponse.CampoInvalido;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(erro -> new CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof MismatchedInputException causa && !causa.getPath().isEmpty()) {
            String campo = causa.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(nome -> nome != null)
                    .collect(Collectors.joining("."));
            String mensagem = "Valor inválido para o campo " + campo + "." + valoresAceitos(causa.getTargetType());
            return responder(HttpStatus.BAD_REQUEST, mensagem, request, List.of(new CampoInvalido(campo, mensagem)));
        }
        return responder(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou mal formatado.", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String mensagem = "Valor inválido para o parâmetro " + ex.getName() + "."
                + valoresAceitos(ex.getRequiredType());
        return responder(HttpStatus.BAD_REQUEST, mensagem, request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "O parâmetro " + ex.getParameterName() + " é obrigatório.",
                request, null);
    }

    @ExceptionHandler({PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErroResponse> ordenacaoInvalida(RuntimeException ex, HttpServletRequest request) {
        Throwable causa = ex instanceof PropertyReferenceException ? ex : ex.getCause();
        if (causa instanceof PropertyReferenceException referencia) {
            return responder(HttpStatus.BAD_REQUEST,
                    "Propriedade de ordenação inválida: " + referencia.getPropertyName() + ".", request, null);
        }
        return erroInesperado(ex, request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Rota não encontrada.", request, null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        LOG.warn("Violação de integridade em {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados (registro duplicado ou dependente).",
                request, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Método " + ex.getMethod() + " não suportado para esta rota.", request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoDeMidia(HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Tipo de conteúdo não suportado. Envie os dados em application/json.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception ex, HttpServletRequest request) {
        LOG.error("Erro não tratado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente mais tarde.", request, null);
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, HttpServletRequest request,
            List<CampoInvalido> campos) {
        ErroResponse corpo = new ErroResponse(
                LocalDateTime.now(clock),
                status.value(),
                descrever(status),
                mensagem,
                request.getRequestURI(),
                campos);
        return ResponseEntity.status(status).body(corpo);
    }

    private static String descrever(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Requisição inválida";
            case NOT_FOUND -> "Recurso não encontrado";
            case CONFLICT -> "Conflito";
            case UNPROCESSABLE_ENTITY -> "Regra de negócio violada";
            default -> "Erro interno";
        };
    }

    private static String valoresAceitos(Class<?> tipo) {
        if (tipo == null || !tipo.isEnum()) {
            return "";
        }
        String valores = Arrays.stream(tipo.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return " Valores aceitos: " + valores + ".";
    }
}
