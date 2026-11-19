package com.github.vitormozer9.pedidosestoque.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.github.vitormozer9.pedidosestoque.controller.EstoqueController;
import com.github.vitormozer9.pedidosestoque.exception.ApiExceptionHandler;
import com.github.vitormozer9.pedidosestoque.service.EstoqueService;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConflitoHttpTest {
    @Test
    void traduzFalhaDeVersaoParaConflito() throws Exception {
        var service = mock(EstoqueService.class);
        when(service.movimentar(eq(1L), any())).thenThrow(new OptimisticLockingFailureException("versão obsoleta"));
        MockMvcBuilders.standaloneSetup(new EstoqueController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build()
                .perform(post("/api/v1/estoques/1/movimentacoes").contentType("application/json")
                        .content("{\"tipo\":\"SAIDA\",\"quantidade\":1}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.erro").isString());
    }
}
