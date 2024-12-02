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
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.medgrand.DBKPProject.Infrastucture.UserRepository;
import ru.medgrand.DBKPProject.Models.Employee;
import ru.medgrand.DBKPProject.Models.User;
import ru.medgrand.DBKPProject.Security.SecurityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Route("/users")
@RolesAllowed("admin")
public class UsersView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserRepository userRepository;

    @Autowired
    public UsersView(SecurityService securityService,
                     UserRepository userRepository){
        this.securityService = securityService;
        this.userRepository = userRepository;


        add(getNavBar());

        Grid<User> grid = getAllUsers();

        Dialog newUser = new Dialog();
        VerticalLayout layout = new VerticalLayout();

        HorizontalLayout userCreator = new HorizontalLayout();
        TextField login = new TextField("Введите логин");
        PasswordField password = new PasswordField("Введите пароль");
        TextField email = new TextField("Введите email");
        TextField telephone = new TextField("Введите телефон");

        userCreator.add(login, password, email, telephone);

        Button createButton = new Button("Создать", e -> {
            User newUs = new User();
            newUs.setCreated_at(LocalDateTime.now());
            newUs.setRole("user");
            newUs.setEmail(email.getValue());
            newUs.setTelephone(telephone.getValue());
            newUs.setUsername(login.getValue());
            newUs.setPassword(new BCryptPasswordEncoder().encode(password.getValue()));

            Optional<User> us = userRepository.createUser(newUs);

            if(us.isEmpty()){
                Notification notification = new Notification("Ошибка создания: возможно уже существует такой аккаунт", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }

            grid.setItems(userRepository.getAllUsers());

        });

        Button close = new Button("Закрыть", e -> newUser.close());

        layout.add(userCreator, createButton, close);
        newUser.add(layout);

        Button openButton = new Button("Создать пользователя", e -> {
            newUser.open();
        });

        add(openButton);
        add(grid);

    }

    private Grid<User> getAllUsers(){

        Grid<User> grid = new Grid<>(User.class);

        grid.setColumns("user_id", "username", "role", "email", "telephone", "created_at");

        grid.setItems(userRepository.getAllUsers());

        grid.addComponentColumn(user -> {

            Dialog redact = new Dialog();
            VerticalLayout layout = new VerticalLayout();

            HorizontalLayout emailChanger = new HorizontalLayout();
            TextField email = new TextField("Введите email");
            Button changeEmail = new Button("Изменить email", e -> {
                user.setEmail(email.getValue());
                userRepository.updateUser(user);
                grid.setItems(userRepository.getAllUsers());
            });

            emailChanger.add(email, changeEmail);
            emailChanger.setAlignItems(Alignment.CENTER);

            HorizontalLayout telephoneChanger = new HorizontalLayout();
            TextField telephone = new TextField("Введите телефон");
            Button changeTelephone = new Button("Изменить телефон", e -> {
                user.setTelephone(telephone.getValue());
                userRepository.updateUser(user);
                grid.setItems(userRepository.getAllUsers());
            });

            telephoneChanger.add(telephone, changeTelephone);
            telephoneChanger.setAlignItems(Alignment.CENTER);

            HorizontalLayout roleChanger = new HorizontalLayout();
            TextField role = new TextField("Введите роль");
            Button changeRole = new Button("Изменить роль", e -> {
                user.setRole(role.getValue());
                if(userRepository.updateUser(user).isEmpty()){
                    Notification notification = new Notification("Ошибка изменения", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
                grid.setItems(userRepository.getAllUsers());
            });

            roleChanger.add(role, changeRole);
            roleChanger.setAlignItems(Alignment.CENTER);

            Button delete = new Button("Удалить пользователя", e -> {
                userRepository.deleteUser(user);
                grid.setItems(userRepository.getAllUsers());
            });
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button close = new Button("Закрыть", e -> redact.close());
            layout.add(emailChanger, telephoneChanger, roleChanger, delete, close);
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
