package ru.medgrand.DBKPProject.Models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {

    private int review_id;
    private User user;
    private Order order;
    private int rating;
    private String comment;
    private LocalDateTime review_date;

}
