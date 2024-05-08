package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class UserFeed {
    private Integer eventId;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer entityId;
    private Long timestamp;
}
