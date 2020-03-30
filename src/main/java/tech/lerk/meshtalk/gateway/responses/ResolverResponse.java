package tech.lerk.meshtalk.gateway.responses;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.meshtalk.entities.Handshake;
import tech.lerk.meshtalk.entities.Message;
import tech.lerk.meshtalk.gateway.managers.db.DatabaseManager;

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
        if (Method.GET.equals(request.getMethod())) {
            log.info("Handling GET request by: '" + request.getClientAddress() + "', url: '" + request.getUrl() + "'");
            if (request.getUrl().startsWith("/messages/id/")) {
                return resolveMessageById(request);
            } else if (request.getUrl().startsWith("/messages/chat/")) {
                return resolveMessagesByChat(request);
            } else if (request.getUrl().startsWith("/messages/sender/")) {
                return resolveMessagesBySender(request);
            } else if (request.getUrl().startsWith("/messages/receiver/")) {
                return resolveMessagesByReceiver(request);
            } else if (request.getUrl().startsWith("/handshakes/id/")) {
                return resolveHandshakeById(request);
            } else if (request.getUrl().startsWith("/handshakes/chat/")) {
                return resolveHandshakesByChat(request);
            } else if (request.getUrl().startsWith("/handshakes/sender/")) {
                return resolveHandshakesBySender(request);
            } else if (request.getUrl().startsWith("/handshakes/receiver/")) {
                return resolveHandshakesByReceiver(request);
            }
        } else if (Method.POST.equals(request.getMethod())) {
            log.info("Handling POST request by: '" + request.getClientAddress() + "', url: '" + request.getUrl() + "'");
            if (request.getUrl().equals("/messages/save")) {
                String[] jsonSplit = request.toString().split("\n\\{");
                String json = "{" + jsonSplit[jsonSplit.length - 1];
                try {
                    Message message = gson.fromJson(json, Message.class);
                    log.info("Got new message: '" + message.getId() + "'!");
                    databaseManager.getMessageDatabaseManager().saveMessage(message);
                    return new Response("Message received!", Status.OK, ContentType.TEXT_PLAIN);
                } catch (JsonSyntaxException e) {
                    log.error("Unable to decode JSON: '" + json + "'", e);
                    return new Response("Unable to decode JSON!", Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
                } catch (SQLException e) {
                    log.error("Unable to save message!", e);
                    return new Response("Unable to save Message!", Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
                }
            } else if (request.getUrl().equals("/handshakes/save")) {
                String[] jsonSplit = request.toString().split("\n\\{");
                String json = "{" + jsonSplit[jsonSplit.length - 1];
                try {
                    Handshake handshake = gson.fromJson(json, Handshake.class);
                    log.info("Got new handshake: '" + handshake.getId() + "'!");
                    databaseManager.getHandshakeDatabaseManager().saveHandshake(handshake);
                    return new Response("Handshake received!", Status.OK, ContentType.TEXT_PLAIN);
                } catch (JsonSyntaxException e) {
                    log.error("Unable to decode JSON: '" + json + "'", e);
                    return new Response("Unable to decode JSON!", Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
                } catch (SQLException e) {
                    log.error("Unable to save message!", e);
                    return new Response("Unable to save Message!", Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
                }
            }
        }
        return new Response("Nothing found!", Status.NOT_FOUND, ContentType.TEXT_PLAIN);
    }

    private Response resolveHandshakeById(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            Handshake handshake = databaseManager.getHandshakeDatabaseManager().getHandshakeById(uuid);
            return new Response(gson.toJson(handshake), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode handshake UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshake found with uuid: '" + uuidString + "'!";
            log.warn(msg);
            return new Response(msg, Status.NOT_FOUND, ContentType.TEXT_PLAIN);
        } catch (SQLException e) {
            String msg = "Error querying the database!";
            log.error(msg, e);
            return new Response(msg, Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
        }
    }

    private Response resolveMessageById(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            Message message = databaseManager.getMessageDatabaseManager().getMessageById(uuid);
            return new Response(gson.toJson(message), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode message UUID: '" + uuidString + "'!";
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

    private Response resolveHandshakesByChat(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            List<Handshake> handshakes = databaseManager.getHandshakeDatabaseManager().getHandshakesForChat(uuid);
            return new Response(gson.toJson(handshakes), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshakes found for chat: '" + uuidString + "'!";
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
            List<Message> messages = databaseManager.getMessageDatabaseManager().getMessagesForChat(uuid);
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

    private Response resolveHandshakesBySender(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            List<Handshake> handshakes = databaseManager.getHandshakeDatabaseManager().getHandshakesForSender(uuid);
            return new Response(gson.toJson(handshakes), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshakes found for sender: '" + uuidString + "'!";
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
            List<Message> messages = databaseManager.getMessageDatabaseManager().getMessagesForSender(uuid);
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

    private Response resolveHandshakesByReceiver(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            List<Handshake> handshakes = databaseManager.getHandshakeDatabaseManager().getHandshakesForReceiver(uuid);
            return new Response(gson.toJson(handshakes), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode UUID: '" + uuidString + "'!";
            log.error(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshakes found for receiver: '" + uuidString + "'!";
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
            List<Message> messages = databaseManager.getMessageDatabaseManager().getMessagesForReceiver(uuid);
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
