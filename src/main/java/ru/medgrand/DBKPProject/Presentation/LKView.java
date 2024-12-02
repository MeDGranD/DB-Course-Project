package ru.medgrand.DBKPProject.Presentation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.medgrand.DBKPProject.Infrastucture.UserRepository;
import ru.medgrand.DBKPProject.Models.User;
import ru.medgrand.DBKPProject.Security.MyUserDetails;
import ru.medgrand.DBKPProject.Security.SecurityService;

@Route("/lk")
@PermitAll
public class LKView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserRepository userRepository;

    @Autowired
    public LKView(SecurityService securityService,
                  UserRepository userRepository){
        this.securityService = securityService;
        this.userRepository = userRepository;

        add(getNavBar());

        HorizontalLayout passwordChanger = new HorizontalLayout();
        PasswordField passwordField = new PasswordField("Введите новый пароль");
        Button changePassword = new Button("Сменить пароль", e -> {
            MyUserDetails userDetails = (MyUserDetails) securityService.getAuthenticatedUser();
            User user = userDetails.getUser();
            user.setPassword(new BCryptPasswordEncoder().encode(passwordField.getValue()));
            userRepository.updateUser(user);
        });

        passwordChanger.add(passwordField, changePassword);
        add(passwordChanger);

        HorizontalLayout emailChanger = new HorizontalLayout();
        TextField emailField = new TextField("Введите новый email");
        Button changeEmail = new Button("Сменить email", e -> {
            MyUserDetails userDetails = (MyUserDetails) securityService.getAuthenticatedUser();
            User user = userDetails.getUser();
            user.setEmail(emailField.getValue());
            userRepository.updateUser(user);
        });

        emailChanger.add(emailField, changeEmail);
        add(emailChanger);

        HorizontalLayout telephoneChanger = new HorizontalLayout();
        PasswordField telephoneField = new PasswordField("Введите новый телефон");
        Button changeTelephone = new Button("Сменить телефон", e -> {
            MyUserDetails userDetails = (MyUserDetails) securityService.getAuthenticatedUser();
            User user = userDetails.getUser();
            user.setTelephone(telephoneField.getValue());
            userRepository.updateUser(user);
        });

        telephoneChanger.add(telephoneField, changeTelephone);
        add(telephoneChanger);

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
