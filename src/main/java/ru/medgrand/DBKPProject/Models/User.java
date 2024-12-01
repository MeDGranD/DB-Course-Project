package ru.medgrand.DBKPProject.Models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private int user_id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String telephone;
    private LocalDateTime created_at;

}
