package ru.medgrand.DBKPProject.Infrastucture;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medgrand.DBKPProject.Models.User;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<User> mapper = (rs, rowNum) ->{
        User user = new User();

        user.setUser_id(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role_name"));
        user.setEmail(rs.getString("email"));
        user.setTelephone(rs.getString("telephone"));
        user.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());

        return user;
    };

    public List<User> getAllUsers(){
        return jdbc.query(
                """
                                    select *
                                    from users
                                    join roles using(role_id)
                                    """,
                mapper
        );
    }

    public Optional<User> getUserById(int id){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            """
                                    select *
                                    from users
                                    join roles using(role_id)
                                    where user_id = ?
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

    public Optional<User> getUserByName(String name){
        try{
            return Optional.ofNullable(
                    jdbc.queryForObject(
                            """
                                    select *
                                    from users
                                    join roles using(role_id)
                                    where username = ?
                                    """,
                            mapper,
                            name
                    )
            );
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<User> createUser(User user){

        Optional<User> existUser = getUserByName(user.getUsername());
        Integer role_id;
        try {
            role_id = jdbc.queryForObject(
                    "select * from roles where role_name = ?",
                    (rs, rowNum) -> {
                        return rs.getInt("role_id");},
                    user.getRole()
            );
        }
        catch (Exception e){
            return Optional.empty();
        }

        if(existUser.isEmpty()){
            jdbc.update(
                    "insert into users (username, password, role_id, email, telephone, created_at) values (?, ?, ?, ?, ?, ?)",
                    user.getUsername(),
                    user.getPassword(),
                    role_id,
                    user.getEmail(),
                    user.getTelephone(),
                    user.getCreated_at()
            );


            return getUserByName(user.getUsername());
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<User> updateUser(User user){

        Integer role_id;
        try {
            role_id = jdbc.queryForObject(
                    "select * from roles where role_name = ?",
                    (rs, rowNum) -> {
                        return rs.getInt("role_id");
                    },
                    user.getRole()
            );
        }
        catch (Exception e){
            return Optional.empty();
        }

        jdbc.update(
                "update users set username = ?, password = ?, role_id = ?, email = ?, telephone = ? where user_id = ?",
                user.getUsername(),
                user.getPassword(),
                role_id,
                user.getEmail(),
                user.getTelephone(),
                user.getUser_id()
        );

        return getUserById(user.getUser_id());
    }

    @Transactional
    public void deleteUser(User user){
        jdbc.update(
                "delete from users where user_id = ?",
                user.getUser_id()
        );
    }

}
