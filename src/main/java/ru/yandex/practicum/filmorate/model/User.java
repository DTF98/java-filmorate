package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class User {
    private Set<Integer> friends;
    private Integer id;
    @NotBlank(message = "электронная почта не может быть пустой")
    @Email(message = "электронная почта должна содержать символ @")
    private String email;
    @NotBlank
    private String login;
    @NonFinal
    private String name;
    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @Builder
    @Jacksonized
    public User(Integer id, String email, String login, String name, LocalDate birthday, Set<Integer> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        if (name == null || name.isBlank()) this.name = login;
        else this.name = name;
        this.birthday = birthday;
        this.friends = friends == null ? new HashSet<>() : friends;
    }

    public void removeFriend(Integer id) {
            friends.remove(id);
    }

    public void setFriend(Integer id) {
        friends.add(id);

    }
}
