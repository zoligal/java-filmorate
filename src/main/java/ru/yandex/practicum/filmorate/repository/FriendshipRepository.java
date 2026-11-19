package ru.yandex.practicum.filmorate.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FriendshipRepository {

    private final JdbcTemplate jdbcTemplate;

    public FriendshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Long> findConfirmedFriends(Long userId) {
        String sql = """
                SELECT CASE WHEN user_id_1 = ? THEN user_id_2 ELSE user_id_1 END
                FROM friendships
                WHERE status = 'CONFIRMED'
                  AND (user_id_1 = ? OR user_id_2 = ?)
                """;
        return jdbcTemplate.queryForList(sql, Long.class, userId, userId, userId);
    }
}
