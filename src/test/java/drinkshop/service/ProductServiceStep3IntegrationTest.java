package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.service.validator.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Lab04 Scenario (1) - Step 3: integrate S + V + R (all real).
 * Uses a per-test temporary file for FileProductRepository.
 */
@DisplayName("Integration Step3 (Top-Down): ProductService + ProductValidator + FileProductRepository")
class ProductServiceStep3IntegrationTest {

    @TempDir
    Path tempDir;

    private Path repoFile;
    private FileProductRepository repo;
    private ProductService service;

    @BeforeEach
    void setUp() throws IOException {
        repoFile = tempDir.resolve("products_test.dat");
        if (Files.notExists(repoFile)) {
            Files.createFile(repoFile);
        }
        // Ensure clean state for each test
        Files.writeString(repoFile, "");

        repo = new FileProductRepository(repoFile.toString());
        service = new ProductService(repo, new ProductValidator());
    }

    @Test
    @DisplayName("Valid product -> validated and persisted in real repository (findAll size=1)")
    void addProduct_valid_shouldPersistInRepo() {
        // Arrange
        Product product = new Product(
                1,
                "Latte",
                15.50,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy"
        );

        // Act
        service.addProduct(product);

        // Assert
        assertEquals(1, repo.findAll().size());
        assertEquals(product.getId(), repo.findOne(1).getId());
    }

    @Test
    @DisplayName("Persistence check -> data survives reloading repository from file")
    void addProduct_valid_shouldBeReloadedFromFile() {
        // Arrange
        Product product = new Product(
                2,
                "Espresso",
                9.99,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy"
        );

        // Act
        service.addProduct(product);

        // Re-create repository (simulates app restart)
        FileProductRepository reloadedRepo = new FileProductRepository(repoFile.toString());

        // Assert
        assertEquals(1, reloadedRepo.findAll().size());
        Product reloaded = reloadedRepo.findOne(2);
        assertNotNull(reloaded);
        assertEquals("Espresso", reloaded.getNume());
        assertEquals(9.99, reloaded.getPret());
    }
}
