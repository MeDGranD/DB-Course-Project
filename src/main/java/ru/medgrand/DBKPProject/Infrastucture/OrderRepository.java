package ru.medgrand.DBKPProject.Infrastucture;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medgrand.DBKPProject.Models.Menu_Item;
import ru.medgrand.DBKPProject.Models.Order;
import ru.medgrand.DBKPProject.Models.User;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private RowMapper<Order> mapper;

    @PostConstruct
    public void init() {
        mapper = (rs, rowNum) -> {
            Order order = new Order();

            order.setOrder_id(rs.getInt("order_id"));
            order.setOrder_date(rs.getTimestamp("order_date").toLocalDateTime());
            order.setTotal_price(rs.getDouble("total_price"));

            Optional<User> user = userRepository.getUserById(rs.getInt("user_id"));

            if (user.isEmpty()) {
                throw new RuntimeException("User don`t exists");
            }

            order.setHistory(
                    jdbc.query(
                            "select * from order_history where order_id = ?",
                            (rs2, rowNum2) -> {
                                Order.Order_History rowHistory = new Order.Order_History();
                                rowHistory.setTime(rs2.getTimestamp("time").toLocalDateTime());
                                rowHistory.setStatus(rs2.getString("status"));
                                return rowHistory;
                            },
                            order.getOrder_id()
                    )
            );

            order.setItems(
                    jdbc.query(
                            "select * from order_items where order_id = ?",
                            (rs2, rowNum2) -> {
                                Order.Order_Item item = new Order.Order_Item();
                                item.setItem(menuRepository.getItemById(rs2.getInt("item_id")).get());
                                item.setQuantity(rs2.getInt("quantity"));
                                return item;
                            },
                            order.getOrder_id()
                    )
            );

            order.setUser(user.get());

            return order;
        };
    }

    public Optional<Order> getOrderById(int id){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            """
                                    select *
                                    from orders
                                    where order_id = ?
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

    public List<Order> getOrdersByUser(User user){
        return jdbc.query(
                """
                select *
                from orders
                where user_id = ?
                """,
                mapper,
                user.getUser_id()
        );
    }

    public List<Order> getAllOrders(){
        return jdbc.query(
                """
                select *
                from orders
                """,
                mapper
        );
    }

    @Transactional
    public Optional<Order> createOrder(Order order){

        KeyHolder key = new GeneratedKeyHolder();
        PreparedStatementCreator psc = conn -> {
            PreparedStatement stmt = conn.prepareStatement("insert into orders (user_id, order_date, total_price) values(?, ?, ?)", new String[]{"order_id"});
            stmt.setInt(1, order.getUser().getUser_id());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getOrder_date()));
            stmt.setDouble(3, order.getTotal_price());
            return stmt;
        };


        jdbc.update(
                psc,
                key
        );

        jdbc.update(
                "insert into order_history (order_id, time, status) values(?, ?, ?)",
                key.getKey().intValue(),
                order.getOrder_date(),
                "created"
        );

        for(var item : order.getItems()){
            jdbc.update(
                    "insert into order_items (order_id, item_id, quantity) values(?, ?, ?)",
                    key.getKey().intValue(),
                    item.getItem().getItem_id(),
                    item.getQuantity()
            );
        }

        return getOrderById(key.getKey().intValue());

    }

    @Transactional
    public void changeOrderStatus(Order order, String status){
        jdbc.update(
                "insert into order_history (order_id, time, status) values(?, ?, ?)",
                order.getOrder_id(),
                LocalDateTime.now(),
                status
        );
    }

    @Transactional
    public void changeOrderItemQuantity(Order order, Menu_Item item, int quantity){
        jdbc.update(
                "update order_items set quantity = ? where order_id = ? and item_id = ?",
                quantity,
                order.getOrder_id(),
                item.getItem_id()
        );
    }

    @Transactional
    public void addOrderItem(Order order, Menu_Item item){

        try{
            jdbc.queryForObject(
                    "select * from order_items where order_id = ? and item_id = ?",
                    (rs, rowNum) -> 1,
                    order.getOrder_id(),
                    item.getItem_id()
            );
        }
        catch (EmptyResultDataAccessException e){
            jdbc.update(
                    "insert into order_items (order_id, item_id, quantity) values(?, ?, ?)",
                    order.getOrder_id(),
                    item.getItem_id(),
                    1
            );
        }

    }

    @Transactional
    public void deleteOrderItem(Order order, Menu_Item item){
        jdbc.update(
                "delete from order_items where order_id = ? and item_id = ?",
                order.getOrder_id(),
                item.getItem_id()
        );
    }

    @Transactional
    public void deleteOrder(Order order){
        jdbc.update(
                "delete from orders where order_id = ?",
                order.getOrder_id()
        );
    }

}
