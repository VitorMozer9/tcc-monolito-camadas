package com.github.vitormozer9.pedidosestoque.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import com.github.vitormozer9.pedidosestoque.entity.TipoMovimentacao;
import com.github.vitormozer9.pedidosestoque.repository.EstoqueRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EstoqueApiTest extends ApiTestBase {
    @Autowired EstoqueRepository estoques;
    @Autowired PlatformTransactionManager transacoes;

    @Test
    void cadastroUnicoEMovimentacoesManuais() throws Exception {
        long id = criarProduto();
        String rota = ESTOQUES + "/" + id;
        String movimento = rota + "/movimentacoes";
        String cadastro = "{\"produtoId\":%d,\"quantidadeInicial\":0}".formatted(id);
        enviar(post(ESTOQUES), cadastro).andExpect(status().isCreated())
                .andExpect(content().json("{\"produtoId\":%d,\"quantidadeDisponivel\":0}".formatted(id),
                        org.springframework.test.json.JsonCompareMode.STRICT));
        enviar(post(ESTOQUES), cadastro).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").isString());
        mvc.perform(delete(PRODUTOS + "/" + id)).andExpect(status().isNoContent());
        enviar(post(movimento), "{\"tipo\":\"ENTRADA\",\"quantidade\":4}").andExpect(status().isOk());
        enviar(post(movimento), "{\"tipo\":\"SAIDA\",\"quantidade\":5}").andExpect(status().isConflict());
        enviar(post(movimento), "{\"tipo\":\"SAIDA\",\"quantidade\":1}").andExpect(status().isOk());
        mvc.perform(get(rota)).andExpect(jsonPath("$.quantidadeDisponivel").value(3));
    }

    @Test
    void validaEntradasERecursos() throws Exception {
        long id = criarProduto();
        enviar(post(ESTOQUES), "{\"produtoId\":%d,\"quantidadeInicial\":-1}".formatted(id))
                .andExpect(status().isBadRequest());
        mvc.perform(get(ESTOQUES + "/" + id)).andExpect(status().isNotFound());
        enviar(post(ESTOQUES + "/" + id + "/movimentacoes"), "{\"tipo\":\"SAIDA\",\"quantidade\":0}")
                .andExpect(status().isBadRequest());
        mvc.perform(delete(PRODUTOS + "/" + id)).andExpect(status().isNoContent());
        enviar(post(ESTOQUES), "{\"produtoId\":%d,\"quantidadeInicial\":1}".formatted(id))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizacaoObsoletaFalhaEPreservaSaldoConfirmado() throws Exception {
        long id = criarProduto();
        enviar(post(ESTOQUES), "{\"produtoId\":%d,\"quantidadeInicial\":10}".formatted(id))
                .andExpect(status().isCreated());
        var externa = new TransactionTemplate(transacoes);
        var interna = new TransactionTemplate(transacoes);
        interna.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        assertThrows(OptimisticLockingFailureException.class, () -> externa.executeWithoutResult(tx -> {
            var obsoleto = estoques.findByProdutoId(id).orElseThrow();
            interna.executeWithoutResult(outra -> estoques.findByProdutoId(id).orElseThrow()
                    .movimentar(TipoMovimentacao.SAIDA, 3));
            obsoleto.movimentar(TipoMovimentacao.SAIDA, 4);
            estoques.flush();
        }));
        mvc.perform(get(ESTOQUES + "/" + id)).andExpect(jsonPath("$.quantidadeDisponivel").value(7));
    }
}
