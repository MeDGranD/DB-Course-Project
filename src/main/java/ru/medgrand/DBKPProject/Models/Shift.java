package ru.medgrand.DBKPProject.Models;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Shift {

    private int shift_id;
    private Employee employee;
    private LocalDate shift_date;
    private LocalTime start_time;
    private LocalTime end_time;

}
