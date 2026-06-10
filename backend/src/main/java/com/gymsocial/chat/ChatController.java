package com.gymsocial.chat;

import com.gymsocial.chat.dto.SendChatMessageRequest;
import com.gymsocial.shared.exception.ValidationException;
import com.gymsocial.shared.http.AuthenticatedUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;

public final class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    public void session(Context context) {
        context.json(service.findSession(
            AuthenticatedUserContext.getUserId(context)
        ));
    }

    public void list(Context context) {
        context.json(service.findMessages(
            AuthenticatedUserContext.getUserId(context),
            context.queryParam("cursor"),
            parseLimit(context)
        ));
    }

    public void send(Context context) {
        context
            .status(HttpStatus.CREATED)
            .json(service.send(
                AuthenticatedUserContext.getUserId(context),
                context.bodyAsClass(SendChatMessageRequest.class)
            ));
    }

    private int parseLimit(Context context) {
        String value = context.queryParam("limit");
        if (value == null || value.isBlank()) {
            return 30;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ValidationException(Map.of(
                "limit",
                "Informe um limite válido."
            ));
        }
    }
}

