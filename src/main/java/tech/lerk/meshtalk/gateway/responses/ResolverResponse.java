package tech.lerk.meshtalk.gateway.responses;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.meshtalk.entities.Message;
import tech.lerk.meshtalk.gateway.managers.DatabaseManager;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ResolverResponse implements IResponse {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ResolverResponse.class);

    private final DatabaseManager databaseManager;
    private final Gson gson;

    public ResolverResponse(DatabaseManager databaseManager, Gson gson) {
        this.databaseManager = databaseManager;
        this.gson = gson;
    }

    @Override
    public Response getResponse(Request request) {
        log.info("Handling request by: '" + request.getClientAddress() + "', url: '" + request.getUrl());
        if (Method.GET.equals(request.getMethod())) {
            if (request.getUrl().startsWith("/id/")) {
                return resolveMessageById(request);
            } else if (request.getUrl().startsWith("/chat/")) {
                return resolveMessagesByChat(request);
            } else if (request.getUrl().startsWith("/sender/")) {
                return resolveMessagesBySender(request);
            } else if (request.getUrl().startsWith("/receiver/")) {
                return resolveMessagesByReceiver(request);
            }
        }
        return new Response("Nothing found!", Status.NOT_FOUND, ContentType.TEXT_PLAIN);
    }

    private Response resolveMessageById(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            Message message = databaseManager.getMessageById(uuid);
            return new Response(gson.toJson(message), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No message found with uuid: '" + uuidString + "'!";
            log.warn(msg);
            return new Response(msg, Status.NOT_FOUND, ContentType.TEXT_PLAIN);
        } catch (SQLException e) {
            String msg = "Error querying the database!";
            log.error(msg, e);
            return new Response(msg, Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
        }
    }

    private Response resolveMessagesByChat(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            List<Message> messages = databaseManager.getMessagesForChat(uuid);
            return new Response(gson.toJson(messages), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No messages found for chat: '" + uuidString + "'!";
            log.warn(msg);
            return new Response(msg, Status.NOT_FOUND, ContentType.TEXT_PLAIN);
        } catch (SQLException e) {
            String msg = "Error querying the database!";
            log.error(msg, e);
            return new Response(msg, Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
        }
    }

    private Response resolveMessagesBySender(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            List<Message> messages = databaseManager.getMessagesForSender(uuid);
            return new Response(gson.toJson(messages), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No messages found for sender: '" + uuidString + "'!";
            log.warn(msg);
            return new Response(msg, Status.NOT_FOUND, ContentType.TEXT_PLAIN);
        } catch (SQLException e) {
            String msg = "Error querying the database!";
            log.error(msg, e);
            return new Response(msg, Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
        }
    }

    private Response resolveMessagesByReceiver(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            List<Message> messages = databaseManager.getMessagesForReceiver(uuid);
            return new Response(gson.toJson(messages), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No messages found for receiver: '" + uuidString + "'!";
            log.warn(msg);
            return new Response(msg, Status.NOT_FOUND, ContentType.TEXT_PLAIN);
        } catch (SQLException e) {
            String msg = "Error querying the database!";
            log.error(msg, e);
            return new Response(msg, Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
        }
    }

    private String parseUuidString(Request request) {
        String[] urlSplit = request.getUrl().split("/");
        return urlSplit[urlSplit.length - 1];
    }
}
