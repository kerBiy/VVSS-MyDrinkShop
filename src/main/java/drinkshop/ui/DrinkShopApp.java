package drinkshop.ui;

import drinkshop.domain.*;
import drinkshop.reports.DailyReportService;
import drinkshop.repository.Repository;
import drinkshop.repository.file.FileOrderRepository;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.repository.file.FileRetetaRepository;
import drinkshop.repository.file.FileStocRepository;
import drinkshop.service.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DrinkShopApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ---------- Repositories ----------
        Repository<Integer, Product> productRepo = new FileProductRepository("data/products.txt");
        Repository<Integer, Order> orderRepo = new FileOrderRepository("data/orders.txt", productRepo);
        Repository<Integer, Reteta> retetaRepo = new FileRetetaRepository("data/retete.txt");
        Repository<Integer, Stoc> stocRepo = new FileStocRepository("data/stocuri.txt");

        // ---------- Services (explicit DI) ----------
        ProductService productService = new ProductService(productRepo);
        OrderService orderService = new OrderService(orderRepo, productRepo);
        RetetaService retetaService = new RetetaService(retetaRepo);
        StocService stocService = new StocService(stocRepo);
        DailyReportService dailyReportService = new DailyReportService(orderService);

        DrinkShopService service = new DrinkShopService(
                productService, orderService, retetaService, stocService, dailyReportService);

        // ---------- FXML ----------
        FXMLLoader loader = new FXMLLoader(getClass().getResource("drinkshop.fxml"));
        Scene scene = new Scene(loader.load());

        DrinkShopController controller = loader.getController();
        controller.setService(service);

        stage.setTitle("Coffee Shop Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
