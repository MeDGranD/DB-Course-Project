package ru.medgrand.DBKPProject.Infrastucture;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medgrand.DBKPProject.Models.Employee;
import ru.medgrand.DBKPProject.Models.Shift;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShiftRepository {

    private final JdbcTemplate jdbc;
    private final EmployeeRepository employeeRepository;
    private RowMapper<Shift> mapper;

    @PostConstruct
    public void init() {
        mapper = (rs, rowNum) -> {
            Shift shift = new Shift();

            shift.setShift_id(rs.getInt("shift_id"));
            shift.setStart_time(rs.getTime("start_time").toLocalTime());
            shift.setEnd_time(rs.getTime("end_time").toLocalTime());
            shift.setShift_date(rs.getDate("shift_date").toLocalDate());

            Optional<Employee> employee = employeeRepository.getEmployeeById(rs.getInt("employee_id"));

            if (employee.isEmpty()) {
                throw new RuntimeException("User don`t exists");
            }

            shift.setEmployee(employee.get());

            return shift;
        };
    }

    public Optional<Shift> getShiftsById(int id){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            """
                                    select *
                                    from shifts
                                    where shift_id = ?
                                    """,
                            mapper,
                            id
                    )
            );
        }
        catch (Exception e){
            return Optional.empty();
        }
    }

    public List<Shift> getAllShifts(){
        return jdbc.query(
                "select * from shifts",
                mapper
        );
    }

    public List<Shift> getShiftsByEmployee(Employee employee){
        return jdbc.query(
                    """
                    select *
                    from shifts
                    where employee_id = ?
                    """,
                    mapper,
                    employee.getEmployee_id()
            );
    }

    public List<Shift> getShiftsByDate(LocalDate date){
        return jdbc.query(
                """
                select *
                from shifts
                where shift_date = ?
                """,
                mapper,
                date
        );
    }

    @Transactional
    public Optional<Shift> createShift(Shift shift){
        try {
            Shift shiftInDate = jdbc.queryForObject(
                    "select * from shifts where shift_date = ? and employee_id = ?",
                    mapper,
                    shift.getShift_date(),
                    shift.getEmployee().getEmployee_id()
            );
            return Optional.empty();
        }
        catch (EmptyResultDataAccessException e) {
            jdbc.update(
                    "insert into shifts (employee_id, shift_date, start_time, end_time) values (?, ?, ?, ?)",
                    shift.getEmployee().getEmployee_id(),
                    shift.getShift_date(),
                    shift.getStart_time(),
                    shift.getEnd_time()
            );

            return Optional.ofNullable(jdbc.queryForObject(
                    "select * from shifts where shift_date = ? and employee_id = ?",
                    mapper,
                    shift.getShift_date(),
                    shift.getEmployee().getEmployee_id()
            ));
        }
    }

    @Transactional
    public Optional<Shift> updateShift(Shift shift){
        jdbc.update("update shifts set employee_id = ?, shift_date = ?, start_time = ?, end_time = ? where shift_id = ?",
                shift.getEmployee().getEmployee_id(),
                shift.getShift_date(),
                shift.getStart_time(),
                shift.getEnd_time(),
                shift.getShift_id()
        );

        return getShiftsById(shift.getShift_id());
    }

    @Transactional
    public void deleteShift(Shift shift){
        jdbc.update(
                "delete from shifts where shift_id = ?",
                shift.getShift_id()
        );
    }

}
