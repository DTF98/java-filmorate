package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private UserStorage storage;

    @BeforeEach
    void create() {
        storage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    public void testFindUserByIdAndCreateUser() {
        User newUser = storage.add(new User(1, "user@email.ru", "vanya123", "Ivan Petrov",
                LocalDate.of(1990, 1, 1)));

        User savedUser = storage.getById(newUser.getId()).get();

        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser);

        assertThrows(NotFoundException.class, () -> storage.getById(10));
    }

    @Test
    public void testAddGetRemoveFriend() {
        User newUser1 = storage.add(new User(1, "1@mail.ru", "DTF1","Denis1",
                LocalDate.of(2001,11,11)));
        User newUser2 = storage.add(new User(2, "2@mail.ru", "DTF2","Denis2",
                LocalDate.of(2001,11,12)));

        storage.addFriend(newUser1.getId(), newUser2.getId());

        List<User> friends1 = storage.getFriends(newUser1.getId());
        assertThat(friends1.get(0))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(newUser2);

        storage.removeFriend(newUser1.getId(),newUser2.getId());

        List<User> friends0 = storage.getFriends(newUser1.getId());
        assertEquals(0, friends0.size());
    }

    @Test
    public void testUpdateUser() {
        User newUser1 = storage.add(new User(1, "1@mail.ru", "DTF1","Denis1",
                LocalDate.of(2001,11,11)));
        User userUpdate = storage.update(new User(newUser1.getId(), "2@mail.ru", "DTF2","Denis2",
                LocalDate.of(2001,11,12)));

        User newUser = storage.getById(userUpdate.getId()).get();
        assertThat(newUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(userUpdate);

        assertThrows(NotFoundException.class, () -> storage.update(new User(2, "1@mail.ru",
                "DTF1","Denis1",
                LocalDate.of(2001,11,11))));
    }
}
