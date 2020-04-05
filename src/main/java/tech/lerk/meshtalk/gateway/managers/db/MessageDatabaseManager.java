package tech.lerk.meshtalk.gateway.managers.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lerk.meshtalk.Utils;
import tech.lerk.meshtalk.entities.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageDatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(MessageDatabaseManager.class);

    private final Connection connection;

    MessageDatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public void saveMessage(Message message) throws SQLException {
        log.info("Saving message: '" + message.getId() + "'");
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO meshtalk_mg.messages " +
                "(uuid_id, uuid_chat, uuid_sender, uuid_receiver, date, content) VALUES (?, ?, ?, ?, ?, ?);");
        preparedStatement.setString(1, message.getId().toString());
        preparedStatement.setString(2, message.getChat().toString());
        preparedStatement.setString(3, message.getSender().toString());
        preparedStatement.setString(4, message.getReceiver().toString());
        preparedStatement.setString(5, Utils.getGson().toJson(message.getDate()));
        preparedStatement.setString(6, message.getContent());
        preparedStatement.executeUpdate();
    }

    public Message getMessageById(UUID messageId) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.messages WHERE uuid_id = ?;");
        preparedStatement.setString(1, messageId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return buildMessage(resultSet);
        } else {
            throw new DatabaseManager.NoResultException("No message found for id: '" + messageId + "'");
        }
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
            throw new DatabaseManager.NoResultException("No messages found for chatId: '" + chatId + "'");
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
            throw new DatabaseManager.NoResultException("No messages found for receiverId: '" + receiverId + "'");
        } else {
            return res;
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
            throw new DatabaseManager.NoResultException("No messages found for senderId: '" + senderId + "'");
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
        m.setDate(Utils.getGson().fromJson(resultSet.getString("date"), LocalDateTime.class));
        m.setContent(resultSet.getString("content"));
        return m;
    }
}
