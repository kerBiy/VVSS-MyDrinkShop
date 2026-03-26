package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ProductService.addProduct - ECP + BVA")
@Tag("unit")
@Tag("lab")
class ProductServiceAddProductTest {

    private ProductService service;
    private InMemoryProductRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryProductRepository();
        service = new ProductService(repo);
    }

    // === ECP NUME (2 cazuri) ===
    
    // ECP 1 - valid class for name
    @Test
    @DisplayName("ECP NUME valid: nume valid (not empty) -> produs adaugat")
    void addProduct_ECP_nume_valid_shouldSave() {
        // Arrange
        Product product = buildProduct("Latte", 15.50);

        // Act
        service.addProduct(product);

        // Assert
        assertEquals(1, repo.findAll().size());
    }

    // ECP 2 - invalid class for name
    @Test
    @DisplayName("ECP NUME invalid: nume empty/blank -> exceptie")
    void addProduct_ECP_nume_invalid_shouldThrow() {
        // Arrange
        Product product = buildProduct("", 15.50);

        // Act + Assert
        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    // === ECP PRICE (2 cazuri) ===
    
    // ECP 3 - valid class for price
    @Test
    @DisplayName("ECP PRICE valid: pret valid (>0) -> produs adaugat")
    void addProduct_ECP_price_valid_shouldSave() {
        // Arrange
        Product product = buildProduct("Espresso", 99.99);

        // Act
        service.addProduct(product);

        // Assert
        assertEquals(1, repo.findAll().size());
    }

    // ECP 4 - invalid class for price
    @Test
    @DisplayName("ECP PRICE invalid: pret negativ/zero -> exceptie")
    void addProduct_ECP_price_invalid_shouldThrow() {
        // Arrange
        Product product = buildProduct("Americano", -1.00);

        // Act + Assert
        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    // === BVA NUME (2 cazuri) ===
    
    // BVA 1 - valid name boundaries
    @ParameterizedTest(name = "BVA NUME valid: lungime = {0}")
    @ValueSource(ints = {1, 2})
    @DisplayName("BVA NUME valid: valori valide la limita (1, 2)")
    void addProduct_BVA_nume_valid_shouldSave(int nameLength) {
        // Arrange
        Product product = buildProduct("N".repeat(nameLength), 10.00);

        // Act
        service.addProduct(product);

        // Assert
        assertEquals(1, repo.findAll().size());
    }

    // BVA 2 - invalid name boundaries (null + blank)
    @ParameterizedTest(name = "BVA NUME invalid: {0}")
    @NullSource
    @ValueSource(strings = {" "})
    @DisplayName("BVA NUME invalid: valori invalide la limita (null, blank)")
    void addProduct_BVA_nume_invalid_shouldThrow(String name) {
        // Arrange
        Product product = buildProduct(name, 10.00);

        // Act + Assert
        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    // === BVA PRICE (2 cazuri) ===
    
    // BVA 3 - valid price boundaries
    @ParameterizedTest(name = "BVA PRICE valid: pret = {0}")
    @ValueSource(doubles = {0.01, 10000.00})
    @DisplayName("BVA PRICE valid: valori valide la limite (0.01, 10000.00)")
    void addProduct_BVA_price_valid_shouldSave(double price) {
        // Arrange
        Product product = buildProduct("Test", price);

        // Act
        service.addProduct(product);

        // Assert
        assertEquals(1, repo.findAll().size());
    }

    // BVA 4 - invalid price boundaries
    @ParameterizedTest(name = "BVA PRICE invalid: pret = {0}")
    @ValueSource(doubles = {0.0, -0.01})
    @DisplayName("BVA PRICE invalid: valori invalide la limita (0.0, -0.01)")
    void addProduct_BVA_price_invalid_shouldThrow(double price) {
        // Arrange
        Product product = buildProduct("Test", price);

        // Act + Assert
        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    private Product buildProduct(String name, double price) {
        return new Product(
                1,
                name,
                price,
                CategorieBautura.CLASSIC_COFFEE,
                TipBautura.BASIC,
                "dummy descriere"
        );
    }

    private static final class InMemoryProductRepository implements Repository<Integer, Product> {

        private final List<Product> store = new ArrayList<>();

        @Override
        public Product findOne(Integer id) {
            return store.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public Product save(Product entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Product delete(Integer id) {
            Product found = findOne(id);
            if (found != null) {
                store.remove(found);
            }
            return found;
        }

        @Override
        public Product update(Product entity) {
            Product existing = findOne(entity.getId());
            if (existing != null) {
                store.remove(existing);
                store.add(entity);
            }
            return entity;
        }
    }
}