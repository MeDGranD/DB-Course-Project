package ru.medgrand.DBKPProject.Models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Order {

    @Data
    public static class Order_History {

        private LocalDateTime time;
        private String status;

    }

    @Data
    public static class Order_Item{

        private Menu_Item item;
        private long quantity;

    }

    private int order_id;
    private List<Order_Item> items = new ArrayList<>();
    private List<Order_History> history = new ArrayList<>();
    private User user;
    private LocalDateTime order_date;
    private double total_price;

}
