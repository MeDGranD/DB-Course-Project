package ru.medgrand.DBKPProject.Infrastucture;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.medgrand.DBKPProject.Models.Menu_Item;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Menu_Item> mapper = (rs, rowNum) -> {
        Menu_Item item = new Menu_Item();

        item.setItem_id(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setAvailable(rs.getBoolean("available"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getDouble("price"));

        return item;
    };

    @Cacheable(value = "menuItems", key = "#id")
    public Optional<Menu_Item> getItemById(int id){
        try{
            return Optional.ofNullable(jdbc.queryForObject(
                    "select * from menu_items where item_id = ?",
                    mapper,
                    id
            ));
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Cacheable(value = "menuItems", key = "#name")
    public Optional<Menu_Item> getItemByName(String name){
        try{
            return Optional.ofNullable(jdbc.queryForObject(
                    "select * from menu_items where name = ?",
                    mapper,
                    name
            ));
        }
        catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Cacheable(value = "allMenuItems", key = "'all'")
    public List<Menu_Item> getAllItems(){
        return jdbc.query(
                "select * from menu_items",
                mapper
        );
    }

    @Transactional
    @CacheEvict(value = "allMenuItems", key = "'all'")
    public Optional<Menu_Item> createItem(Menu_Item item){
        Optional<Menu_Item> existItem = getItemByName(item.getName());

        if(existItem.isEmpty()) {
            jdbc.update(
                    "insert into menu_items (name, description, price, available) values (?, ?, ?, ?)",
                    item.getName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.isAvailable()
            );
            return getItemByName(item.getName());
        }

        return Optional.empty();

    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "menuItems", key = "#item.getItem_id()"),
            @CacheEvict(value = "allMenuItems", key = "'all'")
    })
    public Optional<Menu_Item> updateItem(Menu_Item item){
        jdbc.update(
                "update menu_items set name = ?, description = ?, price = ?, available = ? where item_id = ?",
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.isAvailable(),
                item.getItem_id()
        );

        return getItemByName(item.getName());
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "allMenuItems", key = "'all'"),
                    @CacheEvict(value = "menuItems", key = "#item.getItem_id()")
            }
    )
    public void deleteItem(Menu_Item item) {
        jdbc.update(
                "delete from menu_items where item_id = ?",
                item.getItem_id()
        );
    }
}
