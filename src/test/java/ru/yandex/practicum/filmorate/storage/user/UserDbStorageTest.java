package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    UserDbStorageTest(@Autowired UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    private User makeUser() {
        User user = new User();
        user.setEmail("vasya@example.com");
        user.setLogin("vasya");
        user.setName("Вася");
        user.setBirthday(LocalDate.of(1990, 5, 1));
        return user;
    }

    @Test
    void addShouldSaveAndAssignId() {
        User user = userStorage.add(makeUser());

        assertThat(user.getId()).isNotNull().isPositive();
    }

    @Test
    void getByIdShouldReturnSavedUser() {
        User saved = userStorage.add(makeUser());

        Optional<User> found = userStorage.getById(saved.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(saved.getId());
                    assertThat(u.getEmail()).isEqualTo("vasya@example.com");
                    assertThat(u.getLogin()).isEqualTo("vasya");
                    assertThat(u.getName()).isEqualTo("Вася");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1990, 5, 1));
                });
    }

    @Test
    void getByIdShouldReturnEmptyForMissingUser() {
        assertThat(userStorage.getById(9999)).isEmpty();
    }

    @Test
    void getByIdOrFailShouldThrowForMissingUser() {
        assertThatThrownBy(() -> userStorage.getByIdOrFail(9999))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateShouldChangeFields() {
        User saved = userStorage.add(makeUser());
        saved.setName("Новое имя");
        saved.setEmail("new@example.com");

        User updated = userStorage.update(saved);
        Optional<User> reloaded = userStorage.getById(saved.getId());

        assertThat(updated).isNotNull();
        assertThat(reloaded)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getName()).isEqualTo("Новое имя");
                    assertThat(u.getEmail()).isEqualTo("new@example.com");
                });
    }

    @Test
    void deleteShouldRemoveUser() {
        User saved = userStorage.add(makeUser());

        userStorage.delete(saved.getId());

        assertThat(userStorage.getById(saved.getId())).isEmpty();
    }

    @Test
    void getAllShouldReturnAllUsers() {
        userStorage.add(makeUser());
        User second = makeUser();
        second.setEmail("petya@example.com");
        second.setLogin("petya");
        userStorage.add(second);

        assertThat(userStorage.getAll()).hasSize(2);
    }

    @Test
    void addFriendShouldLinkOneWay() {
        User user1 = userStorage.add(makeUser());
        User user2 = makeUser();
        user2.setEmail("petya@example.com");
        user2.setLogin("petya");
        User saved2 = userStorage.add(user2);

        userStorage.addFriend(user1.getId(), saved2.getId());

        assertThat(userStorage.getFriends(user1.getId()))
                .extracting(User::getId)
                .containsExactly(saved2.getId());
        assertThat(userStorage.getFriends(saved2.getId())).isEmpty();
    }

    @Test
    void removeFriendShouldKeepReverseFriendship() {
        User user1 = userStorage.add(makeUser());
        User user2 = makeUser();
        user2.setEmail("petya@example.com");
        user2.setLogin("petya");
        User saved2 = userStorage.add(user2);
        userStorage.addFriend(user1.getId(), saved2.getId());

        userStorage.removeFriend(saved2.getId(), user1.getId());

        assertThat(userStorage.getFriends(user1.getId()))
                .extracting(User::getId)
                .containsExactly(saved2.getId());
    }

    @Test
    void getCommonFriendsShouldReturnMutualOnly() {
        User user1 = userStorage.add(makeUser());
        User user2 = makeUser();
        user2.setEmail("petya@example.com");
        user2.setLogin("petya");
        User saved2 = userStorage.add(user2);
        User user3 = makeUser();
        user3.setEmail("sasha@example.com");
        user3.setLogin("sasha");
        User saved3 = userStorage.add(user3);

        userStorage.addFriend(user1.getId(), saved3.getId());
        userStorage.addFriend(saved2.getId(), saved3.getId());

        assertThat(userStorage.getCommonFriends(user1.getId(), saved2.getId()))
                .extracting(User::getId)
                .containsExactly(saved3.getId());
    }

}
