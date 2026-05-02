package drinkshop.service;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;
import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;
import drinkshop.service.validator.InsufficientStockException;

import java.util.List;

public class StocService {

    private final Repository<Integer, Stoc> stocRepo;

    public StocService(Repository<Integer, Stoc> stocRepo) {
        this.stocRepo = stocRepo;
    }

    public List<Stoc> getAll() {
        return stocRepo.findAll();
    }

    public void add(Stoc s) {
        stocRepo.save(s);
    }

    public void update(Stoc s) {
        stocRepo.update(s);
    }

    public void delete(int id) {
        stocRepo.delete(id);
    }

    public boolean areSuficient(Reteta reteta) {
        if (reteta.getIngrediente() == null) {
            return true;
        }
        for (IngredientReteta e : reteta.getIngrediente()) {
            String ingredient = e.getDenumire();
            double necesar = e.getCantitate();

            double disponibil = stocRepo.findAll().stream()
                    .filter(s -> s.getIngredient().equalsIgnoreCase(ingredient))
                    .mapToDouble(Stoc::getCantitate)
                    .sum();

            if (disponibil < necesar) {
                return false;
            }
        }
        return true;
    }

    public void consuma(Reteta reteta) {
        // P1: Verificare stoc global
        if (!areSuficient(reteta)) {
            throw new InsufficientStockException(
                    "Stoc insuficient pentru reteta produsului cu id=" + reteta.getId());
        }

        // P2: Verificare existență listă ingrediente (pentru a crește CC)
        if (reteta.getIngrediente() == null) {
            return;
        }

        // P3: Structură repetitivă (Loop)
        for (IngredientReteta e : reteta.getIngrediente()) {

            // P4: Verificare validitate ingredient (pentru a crește CC)
            if (e.getCantitate() > 0) {
                String ingredient = e.getDenumire();
                double necesar = e.getCantitate();

                // Apel către metoda extrasă (fără imbricare)
                proceseazaScadereStoc(ingredient, necesar);
            }
        }
    }

    private void proceseazaScadereStoc(String ingredient, double necesar) {
        List<Stoc> ingredienteStoc = stocRepo.findAll().stream()
                .filter(s -> s.getIngredient().equalsIgnoreCase(ingredient))
                .toList();

        double ramas = necesar;

        for (Stoc s : ingredienteStoc) {
            if (ramas <= 0) break;

            double deScazut = Math.min(s.getCantitate(), ramas);
            s.setCantitate((int) (s.getCantitate() - deScazut));
            ramas -= deScazut;

            stocRepo.update(s);
        }
    }
}
