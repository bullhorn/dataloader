package com.bullhorn.dataloader.service.util;

import java.util.UUID;

import com.bullhorn.dataloader.domain.User;

public class GUIDService {

    public static void inflateUser(User user) {
        user.setUsername(UUID.randomUUID().toString());
        user.setPassword("password");
    }
}
