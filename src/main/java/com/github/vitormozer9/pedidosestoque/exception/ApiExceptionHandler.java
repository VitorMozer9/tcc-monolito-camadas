package com.github.vitormozer9.pedidosestoque.exception;

import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> naoEncontrado(RecursoNaoEncontradoException erro) {
        return Map.of("erro", erro.getMessage());
    }

    @ExceptionHandler(ConflitoDeNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflito(ConflitoDeNegocioException erro) {
        return Map.of("erro", erro.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> atualizacaoConcorrente() {
        return Map.of("erro", "O registro foi alterado por outra transação.");
    }
}
