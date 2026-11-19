package com.github.vitormozer9.pedidosestoque.arquitetura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import com.github.vitormozer9.pedidosestoque.PedidosestoqueApplication;
import com.github.vitormozer9.pedidosestoque.service.PedidoService;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CamadasTest {
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackagesOf(PedidosestoqueApplication.class);

    @Test
    void controllersUsamServicesParaAcessarDominioEPersistencia() {
        proibir("..controller..", "..repository..", "..entity..", "jakarta.persistence..", "org.springframework.data..");
    }

    @Test
    void servicesIndependemDeHttp() {
        proibir("..service..", "..controller..", "org.springframework.web..", "org.springframework.http..");
    }

    @Test
    void persistenciaEDominioIndependemDasCamadasSuperiores() {
        proibir("..repository..", "..controller..", "..service..", "..dto..");
        proibir("..entity..", "..controller..", "..service..", "..repository..", "..dto..");
    }

    @Test
    void pedidoIndependeDeEstoque() {
        noClasses().that().haveFullyQualifiedName(PedidoService.class.getName())
                .should().dependOnClassesThat().haveSimpleNameStartingWith("Estoque").check(CLASSES);
    }

    private void proibir(String origem, String... destinos) {
        noClasses().that().resideInAPackage(origem)
                .should().dependOnClassesThat().resideInAnyPackage(destinos).check(CLASSES);
    }
}
