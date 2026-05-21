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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit test (Mockito) for ProductService - Scenario (1): V <- S -> R")
class ProductServiceMockitoUnitTest {

    @Mock
    private ProductValidator validator;

    @Mock
    private FileProductRepository productRepo;

    @InjectMocks
    private ProductService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("addProduct: success -> validator called, repo.save called once")
    void addProduct_success_shouldValidateAndSaveOnce() {
        // Arrange
        Product product = new Product(
                1,
                "Latte",
                15.50,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy"
        );

        doNothing().when(validator).validate(product);
        when(productRepo.save(product)).thenReturn(product);

        // Act + Assert (assert)
        assertDoesNotThrow(() -> service.addProduct(product));

        // Assert (verify)
        verify(validator, times(1)).validate(product);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo, times(1)).save(captor.capture());
        assertEquals(product, captor.getValue());
    }

    @Test
    @DisplayName("addProduct: validator throws -> exception propagated, repo.save never called")
    void addProduct_validatorThrows_shouldNotSave() {
        // Arrange
        Product product = new Product(
                1,
                "Espresso",
                10.00,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy"
        );

        doThrow(new ValidationException("invalid product"))
                .when(validator)
                .validate(any(Product.class));

        // Act + Assert
        assertThrows(ValidationException.class, () -> service.addProduct(product));

        // Verify
        verify(validator, times(1)).validate(product);
        verify(productRepo, never()).save(any(Product.class));
    }
}
