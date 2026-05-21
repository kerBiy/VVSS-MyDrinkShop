package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Lab04 Scenario (1) - Step 2: integrate S + V (real), keep R mocked.
 * S = ProductService (real)
 * V = ProductValidator (real)
 * R = FileProductRepository (mock)
 */
@DisplayName("Integration Step2 (Top-Down): ProductService + ProductValidator, repo mocked")
class ProductServiceStep2IntegrationTest {

    @Mock
    private FileProductRepository productRepo;

    private ProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ProductService(productRepo, new ProductValidator());
    }

    @Test
    @DisplayName("Valid product -> passes real validator and calls repo.save once")
    void addProduct_valid_shouldSaveOnce() {
        // Arrange
        Product product = new Product(
                1,
                "Latte",
                15.50,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy"
        );
        when(productRepo.save(product)).thenReturn(product);

        // Act + Assert
        assertDoesNotThrow(() -> service.addProduct(product));

        // Verify
        verify(productRepo, times(1)).save(product);
    }

    @Test
    @DisplayName("Invalid product (id<=0) -> real validator throws, repo.save never called")
    void addProduct_invalid_shouldThrowAndNotSave() {
        // Arrange: passes ProductService pre-checks (name not blank, price>0), fails in ProductValidator (id<=0)
        Product product = new Product(
                0,
                "Latte",
                10.00,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy"
        );

        // Act + Assert
        assertThrows(ValidationException.class, () -> service.addProduct(product));

        // Verify
        verify(productRepo, never()).save(product);
    }
}
