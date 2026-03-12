package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService {

    private final Repository<Integer, Product> productRepo;
    private final ProductValidator validator = new ProductValidator();

    public ProductService(Repository<Integer, Product> productRepo) {
        this.productRepo = productRepo;
    }

    public void addProduct(Product p) {
        if (p.getNume() == null || p.getNume().isBlank())
            throw new ValidationException("Numele produsului nu poate fi gol!");
        if (p.getPret() <= 0)
            throw new ValidationException("Pretul trebuie sa fie pozitiv!");
        validator.validate(p);
        productRepo.save(p);
    }

    public void updateProduct(int id, String name, double price,
                              CategorieBautura categorie, TipBautura tip,
                              String descriere) {
        if (name == null || name.isBlank())
            throw new ValidationException("Numele produsului nu poate fi gol!");
        if (price <= 0)
            throw new ValidationException("Pretul trebuie sa fie pozitiv!");
        Product updated = new Product(id, name, price, categorie, tip, descriere);
        validator.validate(updated);
        productRepo.update(updated);
    }

    public void deleteProduct(int id) {
        productRepo.delete(id);
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product findById(int id) {
        return productRepo.findOne(id);
    }

    public List<Product> filterByCategorie(CategorieBautura categorie) {
        if (categorie == CategorieBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getCategorie() == categorie)
                .collect(Collectors.toList());
    }

    public List<Product> filterByTip(TipBautura tip) {
        if (tip == TipBautura.ALL) return getAllProducts();
        return getAllProducts().stream()
                .filter(p -> p.getTip() == tip)
                .collect(Collectors.toList());
    }
}
