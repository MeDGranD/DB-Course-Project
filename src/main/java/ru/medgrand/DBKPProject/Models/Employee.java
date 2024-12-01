package ru.medgrand.DBKPProject.Models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Employee {

    private int employee_id;
    private User user;
    private String first_name;
    private String last_name;
    private LocalDate hired_at;
    private double salary;
}
