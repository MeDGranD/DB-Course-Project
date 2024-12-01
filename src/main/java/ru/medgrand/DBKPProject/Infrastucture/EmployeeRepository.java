package ru.medgrand.DBKPProject.Infrastucture;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medgrand.DBKPProject.Models.Employee;
import ru.medgrand.DBKPProject.Models.User;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeRepository {

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;

    private final RowMapper<Employee> mapper = (rs, rowMapper) -> {
        Employee employee = new Employee();

        employee.setEmployee_id(rs.getInt("employee_id"));
        employee.setSalary(rs.getDouble("salary"));
        employee.setFirst_name(rs.getString("first_name"));
        employee.setLast_name(rs.getString("last_name"));
        employee.setHired_at(rs.getDate("hired_at").toLocalDate());

        Optional<User> empUser = userRepository.getUserById(rs.getInt("user_id"));

        if(empUser.isEmpty()){
            throw new RuntimeException("User don`t exists");
        }

        employee.setUser(empUser.get());

        return employee;
    };

    public Optional<Employee> getEmployeeById(int id){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            """
                                    select *
                                    from employees
                                    where employee_id = ?
                                    """,
                            mapper,
                            id
                    )
            );
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public Optional<Employee> getEmployeeByName(String name){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            """
                                    select *
                                    from employees
                                    where first_name = ?
                                    """,
                            mapper,
                            name
                    )
            );
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Employee> createEmployee(Employee employee){
        Optional<Employee> existEmployee = getEmployeeByName(employee.getFirst_name());

        if(existEmployee.isEmpty()){
            jdbc.update(
                    "insert into employees (user_id, first_name, last_name, hired_at, salary,) values (?, ?, ?, ?, ?)",
                    employee.getUser().getUser_id(),
                    employee.getFirst_name(),
                    employee.getLast_name(),
                    employee.getHired_at(),
                    employee.getSalary()
            );

            return getEmployeeByName(employee.getFirst_name());
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<Employee> updateEmployee(Employee employee){
        jdbc.update(
                "update employees set user_id = ?, first_name = ?, last_name = ?, hired_at = ?, salary = ? where employee_id = ?",
                employee.getUser().getUser_id(),
                employee.getFirst_name(),
                employee.getLast_name(),
                employee.getHired_at(),
                employee.getSalary(),
                employee.getEmployee_id()
        );
        return getEmployeeByName(employee.getFirst_name());
    }

    @Transactional
    public void deleteEmployee(Employee employee){
        jdbc.update(
                "delete from employees where employee_id = ?",
                employee.getEmployee_id()
        );
    }

}