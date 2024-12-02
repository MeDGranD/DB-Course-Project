package ru.medgrand.DBKPProject.Models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private int user_id;
    private String username = null;
    private String password = null;
    private String role = "user";
    private String email = null;
    private String telephone = null;
    private LocalDateTime created_at = null;

}
