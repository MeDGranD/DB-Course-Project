package ru.medgrand.DBKPProject.Infrastucture;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medgrand.DBKPProject.Models.Order;
import ru.medgrand.DBKPProject.Models.Review;
import ru.medgrand.DBKPProject.Models.User;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private final RowMapper<Review> mapper = (rs, rowNum) -> {
      Review review = new Review();

      review.setReview_id(rs.getInt("review_id"));
      review.setRating(rs.getInt("rating"));
      review.setReview_date(rs.getTimestamp("review_date").toLocalDateTime());
      review.setComment(rs.getString("comment"));

      Optional<User> user = userRepository.getUserById(rs.getInt("user_id"));
      Optional<Order> order = orderRepository.getOrderById(rs.getInt("order_id"));

      if(user.isEmpty() || order.isEmpty()){
          throw new RuntimeException("User or order don`t exists");
      }

      review.setUser(user.get());
      review.setOrder(order.get());

      return review;
    };

    public List<Review> getReviewsByUser(User user){
        return jdbc.query(
                "select * from reviews where user_id = ?",
                mapper,
                user.getUser_id()
        );
    }

    public Optional<Review> getReviewByOrder(Order order){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            "select * from reviews where order_id = ?",
                            mapper,
                            order.getOrder_id()
                    )
            );
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public Optional<Review> getReviewById(int id){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            "select * from reviews where review_id = ?",
                            mapper,
                            id
                    )
            );
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Review> createReview(Review review){

        Optional<Review> existReview = getReviewByOrder(review.getOrder());

        if(existReview.isEmpty()){
            jdbc.update(
                    "insert into reviews (user_id, order_id, rating, comment, review_date) values(?, ?, ?, ?, ?)",
                    review.getUser().getUser_id(),
                    review.getOrder().getOrder_id(),
                    review.getRating(),
                    review.getComment(),
                    review.getReview_date()
            );
        }

        return getReviewByOrder(review.getOrder());

    }

    @Transactional
    public Optional<Review> updateReview(Review review){
        jdbc.update(
                "update reviews set user_id = ?, order_id = ?, rating = ?, comment = ?, review_date = ? where order_id = ?",
                review.getOrder().getOrder_id()
        );
        return getReviewByOrder(review.getOrder());
    }

    @Transactional
    public void deleteReview(Review review){
        jdbc.update(
                "delete from reviews where review_id = ?",
                review.getReview_id()
        );
    }

}
