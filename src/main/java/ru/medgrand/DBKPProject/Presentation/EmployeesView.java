package ru.medgrand.DBKPProject.Presentation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import ru.medgrand.DBKPProject.Infrastucture.EmployeeRepository;
import ru.medgrand.DBKPProject.Infrastucture.UserRepository;
import ru.medgrand.DBKPProject.Models.Employee;
import ru.medgrand.DBKPProject.Models.User;
import ru.medgrand.DBKPProject.Security.SecurityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Route("/employees")
@RolesAllowed("admin")
public class EmployeesView extends VerticalLayout {

    private final SecurityService securityService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Autowired
    public EmployeesView(SecurityService securityService,
                         EmployeeRepository employeeRepository,
                         UserRepository userRepository){

        this.securityService = securityService;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;

        add(getNavBar());
        Grid<Employee> grid = getAllEmployees();

        Dialog newEmployee = new Dialog();
        VerticalLayout layout = new VerticalLayout();

        HorizontalLayout nameChanger = new HorizontalLayout();
        TextField fistName = new TextField("Введите имя");
        TextField lastName = new TextField("Введите фамилию");
        TextField user_id = new TextField("Введите user_id");

        nameChanger.add(fistName, lastName, user_id);

        NumberField salary = new NumberField("Введите зарплату");
        DatePicker datePicker = new DatePicker("Выберите дату");

        Button createButton = new Button("Создать", e -> {
            Employee newEmp = new Employee();
            newEmp.setSalary(salary.getValue());
            newEmp.setFirst_name(fistName.getValue());
            newEmp.setLast_name(lastName.getValue());

            Optional<User> user = userRepository.getUserById(Integer.parseInt(user_id.getValue()));
            LocalDate date = datePicker.getValue();
            if(date == null)
                date = LocalDate.now();

            if(user.isPresent()){
                newEmp.setUser(user.get());
                newEmp.setHired_at(date);
                User temp = user.get();
                temp.setRole("employee");
                userRepository.updateUser(temp);

                Optional<Employee> emp = employeeRepository.createEmployee(newEmp);

                if(emp.isEmpty()){
                    Notification notification = new Notification("Ошибка создания: возможно уже существует такой аккаунт", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }

                grid.setItems(employeeRepository.getAllEmployees());

            }
            else {
                Notification notification = new Notification("Пользователя с таким user_id не сущетсвует", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }

        });

        Button close = new Button("Закрыть", e -> newEmployee.close());

        layout.add(nameChanger, salary, datePicker, createButton, close);
        newEmployee.add(layout);

        Button openButton = new Button("Создать работника", e -> {
            newEmployee.open();
        });

        add(openButton);
        add(grid);

    }

    private Grid<Employee> getAllEmployees(){

        Grid<Employee> grid = new Grid<>(Employee.class);
        grid.setColumns("employee_id", "first_name", "last_name", "hired_at", "salary");
        grid.addColumn(employee -> employee.getUser().getUser_id()).setHeader("user_id");

        grid.setItems(employeeRepository.getAllEmployees());

        grid.addComponentColumn(employee -> {

            Dialog redact = new Dialog();
            VerticalLayout layout = new VerticalLayout();

            HorizontalLayout nameChanger = new HorizontalLayout();
            TextField fistName = new TextField("Введите имя");
            TextField lastName = new TextField("Введите фамилию");
            Button changeName = new Button("Изменить имя/фамилию", e -> {
                employee.setFirst_name(fistName.getValue());
                employee.setLast_name(lastName.getValue());
                employeeRepository.updateEmployee(employee);
                grid.setItems(employeeRepository.getAllEmployees());
            });

            nameChanger.add(fistName, lastName, changeName);

            HorizontalLayout salaryChanger = new HorizontalLayout();
            NumberField salary = new NumberField("Введите зарплату");
            Button setSalary = new Button("Изменить зарплату", e -> {
               employee.setSalary(salary.getValue());
               employeeRepository.updateEmployee(employee);
               grid.setItems(employeeRepository.getAllEmployees());
            });

            salaryChanger.add(salary, setSalary);

            HorizontalLayout hiredChanger = new HorizontalLayout();
            DatePicker datePicker = new DatePicker("Выберите дату");
            Button setHired = new Button("Изменить дату", e -> {
                LocalDate date = datePicker.getValue();
                if(date == null)
                    date = LocalDate.now();
                employee.setHired_at(date);
                employeeRepository.updateEmployee(employee);
                grid.setItems(employeeRepository.getAllEmployees());
            });

            hiredChanger.add(datePicker, setHired);

            Button delete = new Button("Удалить работника", e -> {
                employeeRepository.deleteEmployee(employee);
                grid.setItems(employeeRepository.getAllEmployees());
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button close = new Button("Закрыть", e -> redact.close());
            layout.add(nameChanger, salaryChanger, hiredChanger, delete, close);
            redact.add(layout);

            Button button = new Button("Редактировать");
            button.addClickListener(e -> {
                redact.open();
            });
            return button;

        });

        return grid;

    }

    private HorizontalLayout getNavBar(){

        HorizontalLayout layout = new HorizontalLayout();

        Button menuButton = new Button("Меню",
                e -> getUI().ifPresent(ui -> ui.navigate("/menu")));

        Button orderButton = new Button("Заказы",
                e -> getUI().ifPresent(ui -> ui.navigate("/orders")));

        Button employeeButton = new Button("Работники",
                e -> getUI().ifPresent(ui -> ui.navigate("/employees")));

        Button userButton = new Button("Пользователи",
                e -> getUI().ifPresent(ui -> ui.navigate("/users")));

        Button shiftButton = new Button("Смены",
                e -> getUI().ifPresent(ui -> ui.navigate("/shifts")));

        Button reviewButton = new Button("Отзывы",
                e -> getUI().ifPresent(ui -> ui.navigate("/reviews")));

        Button login  = new Button("Войти",
                e -> getUI().ifPresent(ui -> ui.navigate("/login")));

        Button registration  = new Button("Регистрация",
                e -> getUI().ifPresent(ui -> ui.navigate("/register")));

        Button lk  = new Button("Личный кабинет",
                e -> getUI().ifPresent(ui -> ui.navigate("/lk")));

        Button logout  = new Button("Выйти",
                e -> securityService.logout());

        HorizontalLayout rightButtons = new HorizontalLayout();
        HorizontalLayout leftButtons = new HorizontalLayout();

        leftButtons.add(menuButton);

        UserDetails userDetails = securityService.getAuthenticatedUser();

        if(userDetails != null){

            if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
                leftButtons.add(orderButton);
            }
            else if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                leftButtons.add(orderButton);
            }
            else if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                leftButtons.add(orderButton);
                leftButtons.add(employeeButton);
                leftButtons.add(userButton);
                leftButtons.add(shiftButton);
            }
            leftButtons.add(reviewButton);

            rightButtons.add(lk);
            rightButtons.add(logout);

        }
        else{
            rightButtons.add(login);
            rightButtons.add(registration);
        }

        rightButtons.setSpacing(true);

        layout.add(leftButtons);
        layout.setFlexGrow(1, leftButtons);

        layout.add(rightButtons);
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        layout.setWidthFull();

        return layout;
    }

}
