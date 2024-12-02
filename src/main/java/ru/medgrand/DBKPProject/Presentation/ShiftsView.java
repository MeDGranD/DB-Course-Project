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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.medgrand.DBKPProject.Infrastucture.EmployeeRepository;
import ru.medgrand.DBKPProject.Infrastucture.ShiftRepository;
import ru.medgrand.DBKPProject.Models.Employee;
import ru.medgrand.DBKPProject.Models.Shift;
import ru.medgrand.DBKPProject.Models.User;
import ru.medgrand.DBKPProject.Security.SecurityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Route("/shifts")
@RolesAllowed("admin")
public class ShiftsView extends VerticalLayout {

    private final SecurityService securityService;
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ShiftsView(SecurityService securityService,
                      ShiftRepository shiftRepository,
                      EmployeeRepository employeeRepository){
        this.securityService = securityService;
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;


        add(getNavBar());

        Grid<Shift> grid = getAllShifts();

        Dialog newShift = new Dialog();
        VerticalLayout layout = new VerticalLayout();

        HorizontalLayout shiftCreator = new HorizontalLayout();
        NumberField employee_id = new NumberField("Введите employee_id");
        DatePicker date = new DatePicker("Введите дату смены");
        TimePicker start = new TimePicker("Введите время конца смены");
        TimePicker end = new TimePicker("Введите время конца смены");

        shiftCreator.add(employee_id, date, start, end);

        Button createButton = new Button("Создать", e -> {
            Shift newSh = new Shift();
            newSh.setShift_date(date.getValue() == null ? LocalDate.now() : date.getValue());
            newSh.setStart_time(start.getValue() == null ? LocalTime.now() : start.getValue());
            newSh.setEnd_time(end.getValue() == null ? LocalTime.now() : end.getValue());

            Optional<Employee> emp = employeeRepository.getEmployeeById(employee_id.getValue().intValue());

            if(emp.isEmpty()){
                Notification notification = new Notification("Ошибка создания: не существующий работник", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                return;
            }

            newSh.setEmployee(emp.get());

            Optional<Shift> sh = shiftRepository.createShift(newSh);

            if(sh.isEmpty()){
                Notification notification = new Notification("Ошибка создания", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }

            grid.setItems(shiftRepository.getAllShifts());

        });

        Button close = new Button("Закрыть", e -> newShift.close());

        layout.add(shiftCreator, createButton, close);
        newShift.add(layout);

        Button openButton = new Button("Создать смену", e -> {
            newShift.open();
        });

        add(openButton);
        add(grid);
    }

    private Grid<Shift> getAllShifts(){

        Grid<Shift> grid = new Grid<>(Shift.class);
        grid.setColumns("shift_id", "shift_date", "start_time", "end_time");
        grid.addColumn(shifts -> shifts.getEmployee().getEmployee_id()).setHeader("employee_id");

        grid.setItems(shiftRepository.getAllShifts());

        grid.addComponentColumn(shift -> {

            Dialog redact = new Dialog();
            VerticalLayout layout = new VerticalLayout();

            HorizontalLayout dateChanger = new HorizontalLayout();
            DatePicker date = new DatePicker("Введите дату смены");
            TimePicker start = new TimePicker("Введите время конца смены");
            TimePicker end = new TimePicker("Введите время конца смены");
            Button changeDates = new Button("Изменить смену", e -> {
                shift.setShift_date(date.getValue() == null ? LocalDate.now() : date.getValue());
                shift.setStart_time(start.getValue() == null ? LocalTime.now() : start.getValue());
                shift.setEnd_time(end.getValue() == null ? LocalTime.now() : end.getValue());
                shiftRepository.updateShift(shift);
                grid.setItems(shiftRepository.getAllShifts());
            });

            dateChanger.add(start, end, changeDates);

            Button delete = new Button("Удалить смену", e -> {
                shiftRepository.deleteShift(shift);
                grid.setItems(shiftRepository.getAllShifts());
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button close = new Button("Закрыть", e -> redact.close());
            layout.add(dateChanger, delete, close);
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
