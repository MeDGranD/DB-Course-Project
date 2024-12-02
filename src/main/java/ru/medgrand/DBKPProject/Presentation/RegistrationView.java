package ru.medgrand.DBKPProject.Presentation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.medgrand.DBKPProject.Infrastucture.UserRepository;
import ru.medgrand.DBKPProject.Models.User;

import java.time.LocalDateTime;

@Route("/register")
@AnonymousAllowed
public class RegistrationView extends Div {

    private final UserRepository userRepository;

    @Autowired
    RegistrationView(UserRepository userRepository){

        this.userRepository = userRepository;

        TextField usernameField = new TextField("Логин");
        PasswordField passwordField = new PasswordField("Пароль");

        Button registerButton = new Button("Зарегистрироваться", e -> {
            User user = new User();
            user.setCreated_at(LocalDateTime.now());
            user.setUsername(usernameField.getValue());
            user.setPassword(new BCryptPasswordEncoder().encode(passwordField.getValue()));

            if(userRepository.createUser(user).isPresent()) {
                getUI().ifPresent(ui -> ui.navigate("/"));
            }
            else{
                Notification notification = new Notification("Ошибка регистрации: возможно вы использовали существующий логин", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });

        add(
                new Span("Регистрация"),
                usernameField,
                passwordField,
                registerButton
        );
    }

}
