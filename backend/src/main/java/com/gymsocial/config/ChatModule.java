package com.gymsocial.config;

import com.gymsocial.chat.ChatController;
import com.gymsocial.chat.ChatRepository;
import com.gymsocial.chat.ChatService;
import com.gymsocial.shared.id.PublicIdCodec;
import com.gymsocial.shared.storage.ImageStorage;

import javax.sql.DataSource;

public final class ChatModule {

    private ChatModule() {
    }

    public static ChatController create(
        DataSource dataSource,
        ImageStorage imageStorage,
        PublicIdCodec publicIdCodec
    ) {
        return new ChatController(new ChatService(
            new ChatRepository(dataSource),
            imageStorage,
            publicIdCodec
        ));
    }
}

