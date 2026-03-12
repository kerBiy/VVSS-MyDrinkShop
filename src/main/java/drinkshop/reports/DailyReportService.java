package drinkshop.reports;

import drinkshop.service.OrderService;

public class DailyReportService {

    private final OrderService orderService;

    public DailyReportService(OrderService orderService) {
        this.orderService = orderService;
    }

    public double getTotalRevenue() {
        return orderService.getTotalRevenue();
    }

    public int getTotalOrders() {
        return orderService.getAllOrders().size();
    }
}
