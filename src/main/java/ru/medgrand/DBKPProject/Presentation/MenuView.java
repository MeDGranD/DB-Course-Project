package ru.medgrand.DBKPProject.Presentation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import ru.medgrand.DBKPProject.Infrastucture.MenuRepository;
import ru.medgrand.DBKPProject.Infrastucture.OrderRepository;
import ru.medgrand.DBKPProject.Infrastucture.UserRepository;
import ru.medgrand.DBKPProject.Models.Menu_Item;
import ru.medgrand.DBKPProject.Models.Order;
import ru.medgrand.DBKPProject.Security.MyUserDetails;
import ru.medgrand.DBKPProject.Security.SecurityService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("/menu")
@AnonymousAllowed
public class MenuView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;

    private final Map<Integer, Long> cart = new HashMap<>();

    @Autowired
    public MenuView(SecurityService securityService,
                    AuthenticationContext authContext,
                    UserRepository userRepository,
                    MenuRepository menuRepository,
                    OrderRepository orderRepository) {

        this.securityService = securityService;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;

        add(getNavBar());
        add(getAllDishes());

        MyUserDetails userDetails = (MyUserDetails) securityService.getAuthenticatedUser();

        if(userDetails != null)
            add(new Button("Сделать заказ", e -> {
                if(cart.isEmpty()){
                    Notification notification = new Notification("Нельзя сделать заказ с пустой корзиной", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
                else{
                    Order order = new Order();
                    order.setOrder_date(LocalDateTime.now());
                    order.setTotal_price(0);
                    order.setUser(userDetails.getUser());
                    for(var entry : cart.entrySet()){
                        Order.Order_Item item = new Order.Order_Item();
                        Menu_Item menuItem = menuRepository.getItemById(entry.getKey()).get();
                        item.setItem(menuItem);
                        item.setQuantity(entry.getValue());
                        order.getItems().add(item);
                        order.setTotal_price(order.getTotal_price() + item.getQuantity() * menuItem.getPrice());
                    }
                    orderRepository.createOrder(order);
                }
            }));

    }

    private HorizontalLayout getAllDishes(){

        List<Menu_Item> items = menuRepository.getAllItems();
        HorizontalLayout layout = new HorizontalLayout();

        for(var item : items){
            if(item.isAvailable()){
                VerticalLayout tempLayout = new VerticalLayout();

                tempLayout.add(new H4(item.getName() + " - " + item.getPrice() + '$'));
                tempLayout.add(new Span(item.getDescription()));

                HorizontalLayout actions = new HorizontalLayout();

                Button addItem = new Button("Добавить в корзину");

                HorizontalLayout quantityVarifier = new HorizontalLayout();
                Span quantity = new Span("1");
                Button actionButtonMinus = new Button("-", e -> {
                    long newQuantity = cart.get(item.getItem_id()) - 1;
                    if (newQuantity > 0) {
                        cart.put(item.getItem_id(), newQuantity);
                        quantity.setText(String.valueOf(newQuantity));
                    } else {
                        cart.remove(item.getItem_id());
                        actions.removeAll();
                        actions.add(addItem);
                    }
                });
                Button actionButtonPlus = new Button("+", e -> {
                    long newQuantity = cart.getOrDefault(item.getItem_id(), 0L) + 1;
                    cart.put(item.getItem_id(), newQuantity);
                    quantity.setText(String.valueOf(newQuantity));
                });

                quantityVarifier.add(actionButtonMinus, quantity, actionButtonPlus);
                quantityVarifier.setAlignItems(Alignment.CENTER);

                addItem.addClickListener(e -> {
                    cart.put(item.getItem_id(), 1L);
                    actions.removeAll();
                    actions.add(quantityVarifier);
                });

                UserDetails userDetails = securityService.getAuthenticatedUser();

                actions.add(addItem);
                if(userDetails != null)
                    tempLayout.add(actions);
                tempLayout.setWidth(null);
                layout.add(tempLayout);
            }
        }

        layout.getStyle().set("flex-wrap", "wrap");
        layout.setWidth("100%");
        return layout;
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
