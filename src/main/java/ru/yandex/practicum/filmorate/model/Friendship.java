package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;

@Data
public class Friendship {
    private Long requesterId;
    private Long addresseeId;
    private FriendshipStatus status;
}
