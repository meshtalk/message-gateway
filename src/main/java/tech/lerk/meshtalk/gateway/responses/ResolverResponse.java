package tech.lerk.meshtalk.gateway.responses;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.meshtalk.entities.Handshake;
import tech.lerk.meshtalk.entities.Message;
import tech.lerk.meshtalk.entities.Sendable;
import tech.lerk.meshtalk.gateway.managers.db.DatabaseManager;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ResolverResponse extends JSONResponse {
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
        String url = request.getUrl();
        if (Method.GET.equals(request.getMethod())) {
            log.info("GET '" + url + "': '" + request.getClientAddress() + "'");
            if (url.startsWith("/messages/id/")) {
                return resolveMessageById(request);
            } else if (url.startsWith("/messages/chat/")) {
                return resolveMessagesByChat(request);
            } else if (url.startsWith("/messages/sender/")) {
                return resolveMessagesBySender(request);
            } else if (url.startsWith("/messages/receiver/")) {
                return resolveMessagesByReceiver(request);
            } else if (url.startsWith("/handshakes/id/")) {
                return resolveHandshakeById(request);
            } else if (url.startsWith("/handshakes/chat/")) {
                return resolveHandshakesByChat(request);
            } else if (url.startsWith("/handshakes/sender/")) {
                return resolveHandshakesBySender(request);
            } else if (url.startsWith("/handshakes/receiver/")) {
                return resolveHandshakesByReceiver(request);
            }
        } else if (Method.POST.equals(request.getMethod())) {
            log.info("Handling POST request by: '" + request.getClientAddress() + "', url: '" + url + "'");
            switch (url) {
                case "/messages/save":
                    String messageJson = getJSONBody(request);
                    try {
                        return saveSendable(gson.fromJson(messageJson, Message.class));
                    } catch (JsonSyntaxException e) {
                        log.error("Unable to decode JSON: '" + messageJson + "'", e);
                        return new Response("Unable to decode JSON!", Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
                    }
                case "/handshakes/save":
                    String handshakeJson = getJSONBody(request);
                    try {
                        return saveSendable(gson.fromJson(handshakeJson, Handshake.class));
                    } catch (JsonSyntaxException e) {
                        log.error("Unable to decode JSON: '" + handshakeJson + "'", e);
                        return new Response("Unable to decode JSON!", Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
                    }
                case "/admin":
                    return AdminResponse.handle(request);
                default:
                    return new Response("Nothing found!", Status.NOT_FOUND, ContentType.TEXT_PLAIN);
            }
        }
        return new Response("Nothing found!", Status.NOT_FOUND, ContentType.TEXT_PLAIN);
    }


    private Response saveSendable(Sendable sendable) {
        if (sendable instanceof Handshake) {
            try {
                log.info("Got new handshake: '" + sendable.getId() + "'!");
                databaseManager.getHandshakeDatabaseManager().saveHandshake((Handshake) sendable);
                return new Response("Handshake received!", Status.OK, ContentType.TEXT_PLAIN);
            } catch (SQLException e) {
                log.error("Unable to save handshake!", e);
                return new Response("Unable to save handshake!", Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
            }
        } else if (sendable instanceof Message) {
            try {
                log.info("Got new message: '" + sendable.getId() + "'!");
                databaseManager.getMessageDatabaseManager().saveMessage((Message) sendable);
                return new Response("Handshake received!", Status.OK, ContentType.TEXT_PLAIN);
            } catch (SQLException e) {
                log.error("Unable to save message!", e);
                return new Response("Unable to save Message!", Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
            }
        } else {
            return new Response("Unable to save entity!", Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
        }
    }

    private Response resolveHandshakeById(Request request) {
        String uuidString = parseUuidString(request);
        try {
            UUID uuid = UUID.fromString(uuidString);
            Handshake handshake = databaseManager.getHandshakeDatabaseManager().getHandshakeById(uuid);
            return new Response(gson.toJson(handshake), Status.OK, ContentType.APPLICATION_OCTET_STREAM);
        } catch (IllegalArgumentException e) {
            String msg = "Unable to decode handshake UUID: '" + uuidString + "'!";
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshake found with uuid: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No message found with uuid: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshakes found for chat: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No messages found for chat: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshakes found for sender: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No messages found for sender: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No handshakes found for receiver: '" + uuidString + "'!";
            log.debug(msg);
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
            log.debug(msg, e);
            return new Response(msg, Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        } catch (DatabaseManager.NoResultException e) {
            String msg = "No messages found for receiver: '" + uuidString + "'!";
            log.debug(msg);
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
