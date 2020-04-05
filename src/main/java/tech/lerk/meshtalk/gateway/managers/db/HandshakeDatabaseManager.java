package tech.lerk.meshtalk.gateway.managers.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lerk.meshtalk.Utils;
import tech.lerk.meshtalk.entities.Handshake;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HandshakeDatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(HandshakeDatabaseManager.class);

    private final Connection connection;

    HandshakeDatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public void saveHandshake(Handshake handshake) throws SQLException {
        log.info("Saving handshake: '" + handshake.getId() + "'");
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO meshtalk_mg.handshakes " +
                "(uuid_id, uuid_chat, uuid_sender, uuid_receiver, date, key, iv) VALUES (?, ?, ?, ?, ?, ?, ?);");
        preparedStatement.setString(1, handshake.getId().toString());
        preparedStatement.setString(2, handshake.getChat().toString());
        preparedStatement.setString(3, handshake.getSender().toString());
        preparedStatement.setString(4, handshake.getReceiver().toString());
        preparedStatement.setString(5, Utils.getGson().toJson(handshake.getDate()));
        preparedStatement.setString(6, handshake.getKey());
        preparedStatement.setString(6, handshake.getIv());
        preparedStatement.executeUpdate();
    }

    public Handshake getHandshakeById(UUID uuid) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.handshakes WHERE uuid_id = ?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return buildHandshake(resultSet);
        } else {
            throw new DatabaseManager.NoResultException("No handshake found for id: '" + uuid + "'");
        }
    }

    public List<Handshake> getHandshakesForChat(UUID uuid) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.handshakes WHERE uuid_chat = ?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Handshake> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(buildHandshake(resultSet));
        }
        if (res.isEmpty()) {
            throw new DatabaseManager.NoResultException("No handshakes found for chatId: '" + uuid + "'");
        } else {
            return res;
        }
    }

    public List<Handshake> getHandshakesForSender(UUID uuid) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.handshakes WHERE uuid_sender = ?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Handshake> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(buildHandshake(resultSet));
        }
        if (res.isEmpty()) {
            throw new DatabaseManager.NoResultException("No handshakes found for senderId: '" + uuid + "'");
        } else {
            return res;
        }
    }


    public List<Handshake> getHandshakesForReceiver(UUID uuid) throws SQLException {
        PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT * FROM meshtalk_mg.handshakes WHERE uuid_receiver = ?;");
        preparedStatement.setString(1, uuid.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        ArrayList<Handshake> res = new ArrayList<>();
        while (resultSet.next()) {
            res.add(buildHandshake(resultSet));
        }
        if (res.isEmpty()) {
            throw new DatabaseManager.NoResultException("No messages found for receiverId: '" + uuid + "'");
        } else {
            return res;
        }
    }

    private Handshake buildHandshake(ResultSet resultSet) throws SQLException {
        Handshake h = new Handshake();
        h.setId(UUID.fromString(resultSet.getString("uuid_id")));
        h.setChat(UUID.fromString(resultSet.getString("uuid_chat")));
        h.setSender(UUID.fromString(resultSet.getString("uuid_sender")));
        h.setReceiver(UUID.fromString(resultSet.getString("uuid_receiver")));
        h.setDate(Utils.getGson().fromJson(resultSet.getString("date"), LocalDateTime.class));
        h.setKey(resultSet.getString("key"));
        h.setIv(resultSet.getString("iv"));
        return h;
    }
}
