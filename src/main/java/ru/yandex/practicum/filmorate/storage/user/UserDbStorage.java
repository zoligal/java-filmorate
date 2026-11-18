package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper = new UserRowMapper();

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
        }
        return user;
    }

    @Override
    public Optional<User> getById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> result = jdbc.query(sql, mapper, id);
        return result.stream().findFirst();
    }

    @Override
    public User getByIdOrFail(long id) {
        return getById(id)
                .orElseThrow(() -> new ru.yandex.practicum.filmorate.exception.UserNotFoundException(
                        String.format("Пользователь с ID %d не найден", id)));
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rows = jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        if (rows == 0) {
            return null;
        }
        return getById(user.getId()).orElse(null);
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public List<User> getAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", mapper);
    }

    public void addFriend(long userId, long friendId) {
        jdbc.update("MERGE INTO friendships (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)",
                userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        jdbc.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        String sql = """
                SELECT u.* FROM friendships fr
                JOIN users u ON u.id = fr.friend_id
                WHERE fr.user_id = ?
                ORDER BY u.id
                """;
        return jdbc.query(sql, mapper, userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        String sql = """
                SELECT u.* FROM friendships f1
                JOIN friendships f2 ON f1.friend_id = f2.friend_id
                JOIN users u ON u.id = f1.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                ORDER BY u.id
                """;
        return jdbc.query(sql, mapper, userId, otherId);
    }

}
