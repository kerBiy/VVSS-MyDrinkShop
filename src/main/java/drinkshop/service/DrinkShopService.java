package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.export.CsvExporter;
import drinkshop.receipt.ReceiptGenerator;
import drinkshop.reports.DailyReportService;
import drinkshop.service.validator.InsufficientStockException;

import java.util.List;

public class DrinkShopService {

    private final ProductService productService;
    private final OrderService orderService;
    private final RetetaService retetaService;
    private final StocService stocService;
    private final DailyReportService report;

    public DrinkShopService(ProductService productService,
                            OrderService orderService,
                            RetetaService retetaService,
                            StocService stocService,
                            DailyReportService report) {
        this.productService = productService;
        this.orderService = orderService;
        this.retetaService = retetaService;
        this.stocService = stocService;
        this.report = report;
    }

    // ---------- PRODUCT ----------
    public void addProduct(Product p) {
        productService.addProduct(p);
    }

    public void updateProduct(int id, String name, double price,
                              CategorieBautura categorie, TipBautura tip,
                              String descriere) {
        productService.updateProduct(id, name, price, categorie, tip, descriere);
    }

    public void deleteProduct(int id) {
        productService.deleteProduct(id);
    }

    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    public List<Product> filtreazaDupaCategorie(CategorieBautura categorie) {
        return productService.filterByCategorie(categorie);
    }

    public List<Product> filtreazaDupaTip(TipBautura tip) {
        return productService.filterByTip(tip);
    }

    // ---------- ORDER ----------
    public void addOrder(Order o) {
        orderService.addOrder(o);
    }

    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    public double computeTotal(Order o) {
        return orderService.computeTotal(o);
    }

    public String generateReceipt(Order o) {
        return ReceiptGenerator.generate(o, productService.getAllProducts());
    }

    public double getDailyRevenue() {
        return report.getTotalRevenue();
    }

    public void exportCsv(String path) {
        CsvExporter.exportOrders(productService.getAllProducts(),
                orderService.getAllOrders(), path);
    }

    // ---------- STOCK + RECIPE ----------
    /** Verifica stocul si consuma ingredientele. Arunca InsufficientStockException daca stocul e insuficient. */
    public void comandaProdus(Product produs) {
        Reteta reteta = retetaService.findById(produs.getId());
        if (reteta == null) return; // produsul nu are reteta, se considera OK
        if (!stocService.areSuficient(reteta)) {
            throw new InsufficientStockException(
                    "Stoc insuficient pentru produsul: " + produs.getNume());
        }
        stocService.consuma(reteta);
    }

    public List<Reteta> getAllRetete() {
        return retetaService.getAll();
    }

    public void addReteta(Reteta r) {
        retetaService.addReteta(r);
    }

    public void updateReteta(Reteta r) {
        retetaService.updateReteta(r);
    }

    /**
     * Sterge o reteta numai daca nu exista un produs care o foloseste.
     * Integritate: un produs si reteta sa impartasesc acelasi ID.
     */
    public void deleteReteta(int id) {
        boolean productExists = productService.getAllProducts().stream()
                .anyMatch(p -> p.getId() == id);
        if (productExists) {
            throw new IllegalStateException(
                    "Nu se poate sterge reteta: exista un produs asociat cu id=" + id);
        }
        retetaService.deleteReteta(id);
    }
}
