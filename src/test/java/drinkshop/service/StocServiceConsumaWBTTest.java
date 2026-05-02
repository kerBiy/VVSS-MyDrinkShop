package drinkshop.service;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;
import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.service.validator.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StocService.consuma - WBT (F02)")
@Tag("unit")
@Tag("lab")
class StocServiceConsumaWBTTest {

    private StocService service;
    private InMemoryStocRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryStocRepository();
        service = new StocService(repo);
    }

    @Test
    @DisplayName("F02_TC01: stoc insuficient -> InsufficientStockException")
    void consuma_F02_TC01_shouldThrowInsufficientStockException() {
        // Arrange
        repo.save(new Stoc(1, "Zahar", 5, 0));
        repo.save(new Stoc(2, "Lapte", 10, 0));
        Reteta reteta = new Reteta(101, List.of(new IngredientReteta("Zahar", 6)));

        // Act + Assert
        assertThrows(InsufficientStockException.class, () -> service.consuma(reteta));
    }

    @Test
    @DisplayName("F02_TC02: ingrediente = null -> return normal (fara consum)")
    void consuma_F02_TC02_ingredienteNull_shouldReturnWithoutChanges() {
        // Arrange
        repo.save(new Stoc(1, "Orice", 7, 0));
        Reteta reteta = new Reteta(102, null);

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(7, repo.findOne(1).getCantitate());
    }

    @Test
    @DisplayName("F02_TC03: ingrediente = [] -> return normal (loop 0)")
    void consuma_F02_TC03_ingredienteEmpty_shouldDoNothing() {
        // Arrange
        repo.save(new Stoc(1, "Orice", 7, 0));
        Reteta reteta = new Reteta(103, List.of());

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(7, repo.findOne(1).getCantitate());
    }

    @Test
    @DisplayName("F02_TC04: ingredient cu cantitate 0 -> nu consuma")
    void consuma_F02_TC04_cantitateZero_shouldNotConsume() {
        // Arrange
        repo.save(new Stoc(1, "Orice", 7, 0));
        Reteta reteta = new Reteta(104, List.of(new IngredientReteta("Orice", 0)));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(7, repo.findOne(1).getCantitate());
    }

    @Test
    @DisplayName("F02_TC05: ingredient pozitiv, stoc suficient -> consuma")
    void consuma_F02_TC05_shouldConsumeStock() {
        // Arrange
        repo.save(new Stoc(1, "Lapte", 5, 0));
        Reteta reteta = new Reteta(105, List.of(new IngredientReteta("Lapte", 3)));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(2, repo.findOne(1).getCantitate());
    }

    @Test
    @DisplayName("F02_TC06: (2, 0) -> consuma doar cantitatea pozitiva")
    void consuma_F02_TC06_mixedQuantities_shouldConsumeOnlyPositive() {
        // Arrange
        repo.save(new Stoc(1, "Lapte", 5, 0));
        Reteta reteta = new Reteta(106, List.of(
                new IngredientReteta("Lapte", 2),
                new IngredientReteta("Lapte", 0)
        ));

        // Act
        assertDoesNotThrow(() -> service.consuma(reteta));

        // Assert
        assertEquals(3, repo.findOne(1).getCantitate());
    }

    private static final class InMemoryStocRepository implements Repository<Integer, Stoc> {

        private final List<Stoc> store = new ArrayList<>();

        @Override
        public Stoc findOne(Integer id) {
            return store.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
        }

        @Override
        public List<Stoc> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public Stoc save(Stoc entity) {
            store.add(entity);
            return entity;
        }

        @Override
        public Stoc delete(Integer id) {
            Stoc found = findOne(id);
            if (found != null) {
                store.remove(found);
            }
            return found;
        }

        @Override
        public Stoc update(Stoc entity) {
            Stoc existing = findOne(entity.getId());
            if (existing != null) {
                store.remove(existing);
                store.add(entity);
            }
            return entity;
        }
    }
}
