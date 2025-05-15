package ru.medgrand.DBKPProject.Presentation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import ru.medgrand.DBKPProject.Infrastucture.MenuRepository;
import ru.medgrand.DBKPProject.Infrastucture.OrderRepository;
import ru.medgrand.DBKPProject.Infrastucture.UserRepository;
import ru.medgrand.DBKPProject.Models.Menu_Item;
import ru.medgrand.DBKPProject.Models.Order;
import ru.medgrand.DBKPProject.Models.User;
import ru.medgrand.DBKPProject.Pub_Sub.OrderEventPublisher;
import ru.medgrand.DBKPProject.Pub_Sub.OrderEventsSubscriber;
import ru.medgrand.DBKPProject.Pub_Sub.OrderUpdateListener;
import ru.medgrand.DBKPProject.Security.MyUserDetails;
import ru.medgrand.DBKPProject.Security.SecurityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route("/orders")
@PermitAll
public class OrdersView extends VerticalLayout implements OrderUpdateListener {
    private final SecurityService securityService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderEventsSubscriber orderEventsSubscriber;

    private ListDataProvider<Order> dataProvider;;

    @Autowired
    public OrdersView(SecurityService securityService,
                      OrderRepository orderRepository,
                      UserRepository userRepository,
                      MenuRepository menuRepository,
                      OrderEventPublisher orderPublisher,
                      OrderEventsSubscriber orderSubscriber) {

        this.securityService = securityService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.orderEventPublisher = orderPublisher;
        this.orderEventsSubscriber = orderSubscriber;

        this.orderEventsSubscriber.registerListener(this);

        add(getNavBar());
        add(getAllOrders());
    }

    private Grid<Order> getAllOrders(){
        MyUserDetails userDetails = (MyUserDetails) securityService.getAuthenticatedUser();

        Grid<Order> grid = new Grid<>(Order.class);
        grid.setColumns("order_id", "order_date", "total_price");
        grid.addColumn(order -> order.getUser().getUsername()).setHeader("username");
        grid.addColumn(order -> String.join(", ", order.getItems().stream().map(o -> o.getItem().getName() + '[' + o.getQuantity() + ']').toList())).setHeader("items").setSortable(true);
        grid.addColumn(order -> order.getHistory().getLast().getStatus()).setHeader("status");

        if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
            dataProvider = new ListDataProvider<>(new ArrayList<>(
                    orderRepository.getOrdersByUser(userDetails.getUser()))
            );
            grid.setDataProvider(dataProvider);
        }
        if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
            dataProvider = new ListDataProvider<>(new ArrayList<>(
                    orderRepository.getOrdersByUser(userDetails.getUser())).stream().filter(o -> !o.getHistory().getLast().getStatus().equals("taken")).toList()
            );
            grid.setDataProvider(dataProvider);
        }
        if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
            dataProvider = new ListDataProvider<>(new ArrayList<>(
                    orderRepository.getAllOrders())
            );
            grid.setDataProvider(dataProvider);
        }

        grid.addComponentColumn(order -> {

            Dialog redact = new Dialog();
            VerticalLayout layout = new VerticalLayout();

            Button close = new Button("Закрыть", e -> redact.close());
            close.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button delete = new Button("Удалить заказ", e -> {
                if(!order.getHistory().getLast().getStatus().equals("taken")){
                    orderRepository.deleteOrder(order);
                    if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
                        grid.setItems(orderRepository.getOrdersByUser(userDetails.getUser()));
                    }
                    if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                        List<Order> orders = orderRepository.getAllOrders();
                        grid.setItems(orders.stream().filter(o -> !o.getHistory().getLast().getStatus().equals("taken")).toList());
                    }
                    if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                        grid.setItems(orderRepository.getAllOrders());
                    }
                    redact.close();
                    Notification.show("Заказ удален");
                }
                else{
                    Notification notification = new Notification("Нельзя удалить забранный заказ", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
            });

            HorizontalLayout statusLayout = new HorizontalLayout();
            TextField changeStatus = new TextField("Введите статус");
            Button changeStatusButton = new Button("Сменить статус", e -> {
                orderRepository.changeOrderStatus(order, changeStatus.getValue());
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getOrdersByUser(userDetails.getUser()));
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                    List<Order> orders = orderRepository.getAllOrders();
                    grid.setItems(orders.stream().filter(o -> !o.getHistory().getLast().getStatus().equals("taken")).toList());
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getAllOrders());
                }
            });
            statusLayout.add(changeStatus, changeStatusButton);
            statusLayout.setAlignItems(Alignment.CENTER);

            HorizontalLayout itemLayout = new HorizontalLayout();
            TextField itemName = new TextField("Введите название блюда");
            Button deleteItem = new Button("Удалить блюдо", e -> {
                Optional<Menu_Item> item = menuRepository.getItemByName(itemName.getValue());
                if(item.isPresent()){
                    orderRepository.deleteOrderItem(order, item.get());

                }
                else{
                    Notification notification = new Notification("Нельзя удалить несуществующий товар", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getOrdersByUser(userDetails.getUser()));
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                    List<Order> orders = orderRepository.getAllOrders();
                    grid.setItems(orders.stream().filter(o -> !o.getHistory().getLast().getStatus().equals("taken")).toList());
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getAllOrders());
                }
            });
            Button addItem = new Button("Добавить блюдо", e -> {
                Optional<Menu_Item> item = menuRepository.getItemByName(itemName.getValue());
                if(item.isPresent()){
                    orderRepository.addOrderItem(order, item.get());
                }
                else{
                    Notification notification = new Notification("Нельзя добавить несуществующий товар", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getOrdersByUser(userDetails.getUser()));
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                    List<Order> orders = orderRepository.getAllOrders();
                    grid.setItems(orders.stream().filter(o -> !o.getHistory().getLast().getStatus().equals("taken")).toList());
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getAllOrders());
                }
            });
            TextField quantityField = new TextField("Введите количество");
            Button quantityButton = new Button("Изменить количество", e -> {
                Optional<Menu_Item> item = menuRepository.getItemByName(itemName.getValue());
                if(item.isPresent()){
                    orderRepository.changeOrderItemQuantity(order, item.get(), Integer.parseInt(quantityField.getValue()));
                }
                else{
                    Notification notification = new Notification("Нельзя добавить несуществующий товар", 3000);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_user".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getOrdersByUser(userDetails.getUser()));
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                    List<Order> orders = orderRepository.getAllOrders();
                    grid.setItems(orders.stream().filter(o -> !o.getHistory().getLast().getStatus().equals("taken")).toList());
                }
                if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                    grid.setItems(orderRepository.getAllOrders());
                }
            });
            itemLayout.add(itemName, deleteItem, addItem, quantityField, quantityButton);
            itemLayout.setAlignItems(Alignment.CENTER);

            if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_employee".equals(grantedAuthority.getAuthority()))){
                layout.add(statusLayout);
            }

            if(userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> "ROLE_admin".equals(grantedAuthority.getAuthority()))){
                layout.add(statusLayout);
                layout.add(itemLayout);
            }

            layout.add(delete, close);
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

    @Override
    public void onNewOrderReceived(Map<String, Object> orderData) {
        long orderId = ((Number) orderData.get("orderId")).longValue();
        boolean exists = dataProvider.getItems().stream().anyMatch(o -> o.getOrder_id() == orderId);
        if (!exists) {
            orderRepository.getOrderById((int) orderId).ifPresent(newOrder -> {
                dataProvider.getItems().add(newOrder);
                dataProvider.refreshAll();
            });
        }
    }

    @Override
    public void onOrderStatusUpdated(Map<String, Object> statusUpdateData) {

        Long orderId = ((Number) statusUpdateData.get("orderId")).longValue();
        String newStatus = (String) statusUpdateData.get("newStatus");

        Optional<Order> orderToUpdateOpt = dataProvider.getItems().stream()
                .filter(o -> o.getOrder_id() == orderId)
                .findFirst();

        if (orderToUpdateOpt.isPresent()) {
            orderRepository.getOrderById(orderId.intValue()).ifPresent(updatedOrderFromDb -> {
                List<Order> items = new ArrayList<>(dataProvider.getItems());
                items.removeIf(o -> o.getOrder_id() == orderId);
                items.add(updatedOrderFromDb);
                dataProvider = new ListDataProvider<>(items);
                dataProvider.setSortOrder(Order::getOrder_date, SortDirection.DESCENDING);
                dataProvider.refreshAll();
            });
        }
    }
}
