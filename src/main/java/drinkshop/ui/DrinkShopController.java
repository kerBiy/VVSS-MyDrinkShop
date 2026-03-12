package drinkshop.ui;

import drinkshop.domain.*;
import drinkshop.service.DrinkShopService;
import drinkshop.service.validator.InsufficientStockException;
import drinkshop.service.validator.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DrinkShopController {

    private DrinkShopService service;

    // ---------- ROL ----------
    @FXML private ComboBox<Rol> comboRol;
    @FXML private Label lblRolInfo;
    @FXML private VBox panelProduse;
    @FXML private HBox panelRetete;
    @FXML private HBox panelRapoarte;

    public enum Rol { BARMAN, MANAGER }

    // ---------- PRODUCT ----------
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice;
    @FXML private TableColumn<Product, CategorieBautura> colProdCategorie;
    @FXML private TableColumn<Product, TipBautura> colProdTip;
    @FXML private TextField txtProdName, txtProdPrice, txtProdDescriere;
    @FXML private ComboBox<CategorieBautura> comboProdCategorie;
    @FXML private ComboBox<TipBautura> comboProdTip;

    // ---------- RETETE ----------
    @FXML private TableView<Reteta> retetaTable;
    @FXML private TableColumn<Reteta, Integer> colRetetaId;
    @FXML private TableColumn<Reteta, String> colRetetaDesc;

    @FXML private TableView<IngredientReteta> newRetetaTable;
    @FXML private TableColumn<IngredientReteta, String> colNewIngredName;
    @FXML private TableColumn<IngredientReteta, Double> colNewIngredCant;
    @FXML private TextField txtNewIngredName, txtNewIngredCant;

    // ---------- ORDER (CURRENT) ----------
    @FXML private TableView<OrderItem> currentOrderTable;
    @FXML private TableColumn<OrderItem, String> colOrderProdName;
    @FXML private TableColumn<OrderItem, Integer> colOrderQty;

    @FXML private ComboBox<Integer> comboQty;
    @FXML private Label lblOrderTotal;
    @FXML private TextArea txtReceipt;

    @FXML private Label lblTotalRevenue;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Reteta> retetaList = FXCollections.observableArrayList();
    private ObservableList<IngredientReteta> newRetetaList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> currentOrderItems = FXCollections.observableArrayList();

    private Order currentOrder = new Order(1);

    public void setService(DrinkShopService service) {
        this.service = service;
        initData();
    }

    @FXML
    private void initialize() {

        // ROLE SELECTOR
        comboRol.setItems(FXCollections.observableArrayList(Rol.values()));
        comboRol.setValue(Rol.BARMAN);
        applyRole(Rol.BARMAN);
        comboRol.setOnAction(e -> applyRole(comboRol.getValue()));

        // PRODUCTS
        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("pret"));
        colProdCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colProdTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        productTable.setItems(productList);

        comboProdCategorie.getItems().setAll(CategorieBautura.values());
        comboProdTip.getItems().setAll(TipBautura.values());

        // RETETE
        colRetetaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRetetaDesc.setCellValueFactory(data -> {
            Reteta r = data.getValue();
            String desc = r.getIngrediente().stream()
                    .map(i -> i.getDenumire() + " (" + i.getCantitate() + ")")
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(desc);
        });
        retetaTable.setItems(retetaList);

        colNewIngredName.setCellValueFactory(new PropertyValueFactory<>("denumire"));
        colNewIngredCant.setCellValueFactory(new PropertyValueFactory<>("cantitate"));
        newRetetaTable.setItems(newRetetaList);

        // CURRENT ORDER TABLE
        colOrderProdName.setCellValueFactory(data -> {
            int prodId = data.getValue().getProduct().getId();
            Product p = productList.stream().filter(pr -> pr.getId() == prodId).findFirst().orElse(null);
            return new SimpleStringProperty(p != null ? p.getNume() : "N/A");
        });
        colOrderQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        currentOrderTable.setItems(currentOrderItems);

        comboQty.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    private void applyRole(Rol rol) {
        boolean isManager = rol == Rol.MANAGER;
        panelProduse.setVisible(isManager);
        panelProduse.setManaged(isManager);
        panelRetete.setVisible(isManager);
        panelRetete.setManaged(isManager);
        panelRapoarte.setVisible(isManager);
        panelRapoarte.setManaged(isManager);
        lblRolInfo.setText(isManager
                ? "Manager: gestiune produse, retete, rapoarte si comenzi"
                : "Barman: plasare comenzi");
    }

    private void initData() {
        productList.setAll(service.getAllProducts());
        retetaList.setAll(service.getAllRetete());
        lblTotalRevenue.setText("Daily Revenue: " + service.getDailyRevenue());
        updateOrderTotal();
    }

    // ---------- PRODUCT ----------
    @FXML
    private void onAddProduct() {
        Reteta r = retetaTable.getSelectionModel().getSelectedItem();

        if (r == null) {
            showError("Selectati o reteta pentru care adaugati un produs.");
            return;
        }
        if (service.getAllProducts().stream().anyMatch(p -> p.getId() == r.getId())) {
            showWarning("Exista deja un produs asociat acestei retete.");
            return;
        }

        String name = txtProdName.getText();
        String priceText = txtProdPrice.getText();
        String descriere = txtProdDescriere.getText();

        if (name == null || name.isBlank()) { showError("Completati numele produsului."); return; }
        if (priceText == null || priceText.isBlank()) { showError("Completati pretul produsului."); return; }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) { showError("Pretul trebuie sa fie pozitiv."); return; }
            Product p = new Product(r.getId(), name, price,
                    comboProdCategorie.getValue(), comboProdTip.getValue(), descriere);
            service.addProduct(p);
            initData();
        } catch (NumberFormatException ex) {
            showError("Pretul introdus nu este valid.");
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onUpdateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectati un produs din tabel pentru a-l actualiza.");
            return;
        }

        String name = txtProdName.getText();
        String priceText = txtProdPrice.getText();
        String descriere = txtProdDescriere.getText();

        if (name == null || name.isBlank()) { showError("Completati numele produsului."); return; }
        if (priceText == null || priceText.isBlank()) { showError("Completati pretul produsului."); return; }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) { showError("Pretul trebuie sa fie pozitiv."); return; }
            service.updateProduct(selected.getId(), name, price,
                    comboProdCategorie.getValue(), comboProdTip.getValue(), descriere);
            initData();
        } catch (NumberFormatException ex) {
            showError("Pretul introdus nu este valid.");
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selectati un produs din tabel pentru a-l sterge.");
            return;
        }
        service.deleteProduct(selected.getId());
        initData();
    }

    @FXML
    private void onFilterCategorie() {
        productList.setAll(service.filtreazaDupaCategorie(comboProdCategorie.getValue()));
    }

    @FXML
    private void onFilterTip() {
        productList.setAll(service.filtreazaDupaTip(comboProdTip.getValue()));
    }

    // ---------- RETETA NOUA ----------
    @FXML
    private void onAddNewIngred() {
        String ingName = txtNewIngredName.getText();
        String ingCant = txtNewIngredCant.getText();
        if (ingName == null || ingName.isBlank()) { showError("Completati numele ingredientului."); return; }
        try {
            double cantitate = Double.parseDouble(ingCant);
            if (cantitate <= 0) { showError("Cantitatea trebuie sa fie pozitiva."); return; }
            newRetetaList.add(new IngredientReteta(ingName, cantitate));
            txtNewIngredName.clear();
            txtNewIngredCant.clear();
        } catch (NumberFormatException ex) {
            showError("Cantitatea introdusa nu este valida.");
        }
    }

    @FXML
    private void onDeleteNewIngred() {
        IngredientReteta sel = newRetetaTable.getSelectionModel().getSelectedItem();
        if (sel != null) newRetetaList.remove(sel);
    }

    @FXML
    private void onAddNewReteta() {
        if (newRetetaList.isEmpty()) { showError("Adaugati cel putin un ingredient."); return; }
        Reteta r = new Reteta(service.getAllRetete().size() + 1, new ArrayList<>(newRetetaList));
        service.addReteta(r);
        newRetetaList.clear();
        initData();
    }

    @FXML
    private void onClearNewRetetaIngredients() {
        newRetetaList.clear();
        txtNewIngredName.clear();
        txtNewIngredCant.clear();
    }

    // ---------- CURRENT ORDER ----------
    @FXML
    private void onAddOrderItem() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        Integer qty = comboQty.getValue();

        if (selected == null) { showError("Selecteaza un produs din lista."); return; }
        if (qty == null) { showError("Selecteaza cantitatea."); return; }

        currentOrderItems.add(new OrderItem(selected, qty));
        updateOrderTotal();
    }

    @FXML
    private void onDeleteOrderItem() {
        OrderItem sel = currentOrderTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            currentOrderItems.remove(sel);
            updateOrderTotal();
        }
    }

    @FXML
    private void onFinalizeOrder() {
        if (currentOrderItems.isEmpty()) {
            showError("Comanda este goala. Adaugati produse inainte de finalizare.");
            return;
        }

        // Verifica stocul pentru fiecare produs din comanda
        for (OrderItem item : currentOrderItems) {
            try {
                service.comandaProdus(item.getProduct());
            } catch (InsufficientStockException ex) {
                showError("Comanda nu poate fi finalizata:\n" + ex.getMessage());
                return;
            }
        }

        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(currentOrderItems);
        currentOrder.computeTotalPrice();

        service.addOrder(currentOrder);
        txtReceipt.setText(service.generateReceipt(currentOrder));

        currentOrderItems.clear();
        currentOrder = new Order(currentOrder.getId() + 1);
        updateOrderTotal();
    }

    private void updateOrderTotal() {
        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(currentOrderItems);
        double total = service.computeTotal(currentOrder);
        lblOrderTotal.setText("Total: " + total);
    }

    // ---------- EXPORT + REVENUE ----------
    @FXML
    private void onExportOrdersCsv() {
        service.exportCsv("orders.csv");
        showInfo("Export finalizat in fisierul orders.csv");
    }

    @FXML
    private void onDailyRevenue() {
        lblTotalRevenue.setText("Daily Revenue: " + service.getDailyRevenue());
    }

    // ---------- HELPERS ----------
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
