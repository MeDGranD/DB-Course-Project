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
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import ru.medgrand.DBKPProject.Infrastucture.OrderRepository;
import ru.medgrand.DBKPProject.Infrastucture.ReviewRepository;
import ru.medgrand.DBKPProject.Models.Employee;
import ru.medgrand.DBKPProject.Models.Order;
import ru.medgrand.DBKPProject.Models.Review;
import ru.medgrand.DBKPProject.Models.User;
import ru.medgrand.DBKPProject.Security.MyUserDetails;
import ru.medgrand.DBKPProject.Security.SecurityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Route("/reviews")
@PermitAll
public class ReviewsView extends VerticalLayout {

    private final SecurityService securityService;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public ReviewsView(SecurityService securityService,
                       ReviewRepository reviewRepository,
                       OrderRepository orderRepository){
        this.securityService = securityService;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;

        add(getNavBar());

        Grid<Review> grid = getAllReviews();

        Dialog newReview = new Dialog();
        VerticalLayout layout = new VerticalLayout();

        HorizontalLayout reviewChanger = new HorizontalLayout();
        NumberField order_id = new NumberField("Введите order_id");
        NumberField rating = new NumberField("Введите оценку");
        TextField comment = new TextField("Введите отзыв");

        reviewChanger.add(order_id, rating, comment);

        Button createButton = new Button("Создать", e -> {
            Review review = new Review();
            MyUserDetails userDetails = (MyUserDetails)securityService.getAuthenticatedUser();
            review.setReview_date(LocalDateTime.now());
            review.setUser(userDetails.getUser());
            review.setComment(comment.getValue());
            review.setRating(rating.getValue().intValue());

            Optional<Order> order = orderRepository.getOrderById(order_id.getValue().intValue());

            if(order.isPresent()) {
                review.setOrder(order.get());

                if (userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority())) ||
                        userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))) {
                    if (orderRepository.getOrdersByUser(userDetails.getUser()).stream().anyMatch(o -> o.getOrder_id() == order.get().getOrder_id()))
                        reviewRepository.createReview(review);
                    else{
                        Notification notification = new Notification("Вы не делали этот заказ", 3000);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        notification.open();
                    }
                } else {
                    reviewRepository.createReview(review);
                }
                grid.setItems(reviewRepository.getAllReviews());
            }
            else{
                Notification notification = new Notification("Заказа не существует", 3000);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }

        });

        Button close = new Button("Закрыть", e -> newReview.close());

        layout.add(reviewChanger, createButton, close);
        newReview.add(layout);

        Button openButton = new Button("Создать отзыв", e -> {
            newReview.open();
        });

        add(openButton);

        add(grid);

    }

    private Grid<Review> getAllReviews(){

        Grid<Review> grid = new Grid<>(Review.class);
        grid.setColumns("review_id", "rating", "comment", "review_date");
        grid.addColumn(review -> review.getUser().getUser_id()).setHeader("user_id");
        grid.addColumn(review -> review.getOrder().getOrder_id()).setHeader("order_id");

        grid.setItems(reviewRepository.getAllReviews());

        MyUserDetails userDetails = (MyUserDetails) securityService.getAuthenticatedUser();

        if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))) {
            grid.addComponentColumn(review -> {

                Dialog redact = new Dialog();
                VerticalLayout layout = new VerticalLayout();

                HorizontalLayout commentChanger = new HorizontalLayout();
                TextField comment = new TextField("Введите комментарий");
                Button changeComment = new Button("Изменить комментарий", e -> {
                    review.setComment(comment.getValue());
                    reviewRepository.updateReview(review);
                    grid.setItems(reviewRepository.getAllReviews());
                });

                commentChanger.add(comment, changeComment);

                HorizontalLayout ratingChanger = new HorizontalLayout();
                NumberField rating = new NumberField("Введите оценку");
                Button setRating = new Button("Изменить оценку", e -> {
                    review.setRating(rating.getValue().intValue());
                    reviewRepository.updateReview(review);
                    grid.setItems(reviewRepository.getAllReviews());
                });

                ratingChanger.add(rating, setRating);

                Button delete = new Button("Удалить отзыв", e -> {
                    reviewRepository.deleteReview(review);
                    grid.setItems(reviewRepository.getAllReviews());
                });
                delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

                Button close = new Button("Закрыть", e -> redact.close());
                layout.add(commentChanger, ratingChanger, delete, close);
                redact.add(layout);

                Button button = new Button("Редактировать");
                button.addClickListener(e -> {
                    redact.open();
                });
                return button;

            });
        }

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
