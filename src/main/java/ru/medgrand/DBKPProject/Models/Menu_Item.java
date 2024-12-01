package ru.medgrand.DBKPProject.Models;

import lombok.Data;

@Data
public class Menu_Item {

    private int item_id;
    private String name;
    private String description;
    private double price;
    private boolean available;

}
