package tech.lerk.meshtalk.gateway.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lerk.meshtalk.entities.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class that manages database operations.
 *
 * @author Lukas Fülling (lukas@k40s.net)
 */
public class DatabaseManager {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final Connection connection;

    public DatabaseManager(String url, String user, String password) throws SQLException {
        log.info("Connecting to database...");
        connection = DriverManager.getConnection(url, user, password);
    }

    public void saveMessage(Message message) throws SQLException {
        log.info("Saving message: '" + message.getId() + "'");
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO meshtalk_mg.messages " +
                "(uuid_id, uuid_chat, uuid_sender, uuid_receiver, date, content) VALUES (?, ?, ?, ?, ?, ?);");
        preparedStatement.setString(1, message.getId().toString());
        preparedStatement.setString(2, message.getChat().toString());
        preparedStatement.setString(3, message.getSender().toString());
        preparedStatement.setString(4, message.getReceiver().toString());
        preparedStatement.setTime(5, message.getDate());
        preparedStatement.executeUpdate();
    }

    public List<Message> getMessagesForChat(UUID chatId) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.messages WHERE uuid_chat = ?;");
        preparedStatement.setString(1, chatId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Message> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(buildMessage(resultSet));
        }
        if (res.isEmpty()) {
            throw new NoResultException("No messages found for chatId: '" + chatId + "'");
        } else {
            return res;
        }
    }

    public Message getMessageById(UUID messageId) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.messages WHERE uuid_id = ?;");
        preparedStatement.setString(1, messageId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return buildMessage(resultSet);
        } else {
            throw new NoResultException("No message found for id: '" + messageId + "'");
        }
    }

    public List<Message> getMessagesForSender(UUID senderId) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.messages WHERE uuid_sender = ?;");
        preparedStatement.setString(1, senderId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Message> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(buildMessage(resultSet));
        }
        if (res.isEmpty()) {
            throw new NoResultException("No messages found for senderId: '" + senderId + "'");
        } else {
            return res;
        }
    }

    public List<Message> getMessagesForReceiver(UUID receiverId) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.messages WHERE uuid_receiver = ?;");
        preparedStatement.setString(1, receiverId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Message> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(buildMessage(resultSet));
        }
        if (res.isEmpty()) {
            throw new NoResultException("No messages found for receiverId: '" + receiverId + "'");
        } else {
            return res;
        }
    }

    private static Message buildMessage(ResultSet resultSet) throws SQLException {
        Message m = new Message();
        m.setId(UUID.fromString(resultSet.getString("uuid_id")));
        m.setChat(UUID.fromString(resultSet.getString("uuid_chat")));
        m.setSender(UUID.fromString(resultSet.getString("uuid_sender")));
        m.setReceiver(UUID.fromString(resultSet.getString("uuid_receiver")));
        m.setDate(resultSet.getTime("date"));
        m.setContent(resultSet.getString("content"));
        return m;
    }

    public static class NoResultException extends SQLException {
        NoResultException(String message) {
            super(message);
        }
    }
}
