package tech.lerk.meshtalk.gateway;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.meshtalk.Meta;
import tech.lerk.meshtalk.adapters.PrivateKeyTypeAdapter;
import tech.lerk.meshtalk.adapters.PublicKeyTypeAdapter;
import tech.lerk.meshtalk.entities.Message;
import tech.lerk.meshtalk.entities.MetaInfo;
import tech.lerk.meshtalk.gateway.managers.ConfigManager;
import tech.lerk.meshtalk.gateway.managers.DatabaseManager;
import tech.lerk.meshtalk.gateway.responses.ResolverResponse;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.SQLException;

public class Main {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static DatabaseManager databaseManager;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(PrivateKey.class, new PrivateKeyTypeAdapter())
            .registerTypeAdapter(PublicKey.class, new PublicKeyTypeAdapter())
            .create();

    public static void main(String[] args) {

        log.info("Loading configuration...");
        ConfigManager configManager = new ConfigManager();

        log.info("Preparing db...");
        try {
            databaseManager = new DatabaseManager(configManager.getDbConnection(),
                    configManager.getDbUser(), configManager.getDbPassword());
        } catch (SQLException e) {
            log.error("Unable to connect to database!", e);
            System.exit(1);
        }

        log.info("Generating routes...");
        Routes routes = new Routes();
        routes.add(Method.GET, "/", "html/landing.html");
        routes.add(Method.GET, "/style.css", "html/style.css");
        routes.add(Method.GET, "/favicon.ico", "html/favicon.ico");
        routes.add(Method.GET, "/meta", req -> {
            MetaInfo metaInfo = new MetaInfo();
            metaInfo.setApiVersion(Meta.API_VERSION);
            metaInfo.setCoreVersion(Meta.CORE_VERSION);
            return new Response(gson.toJson(metaInfo), ContentType.APPLICATION_OCTET_STREAM);
        });
        routes.add(Method.POST, "/", req -> {
            String[] jsonSplit = req.toString().split("\n\\{");
            String json = "{" + jsonSplit[jsonSplit.length - 1];
            try {
                databaseManager.saveMessage(gson.fromJson(json, Message.class));
                return new Response("Message received!", Status.OK, ContentType.TEXT_PLAIN);
            } catch (JsonSyntaxException e) {
                log.error("Unable to decode JSON: '" + json + "'", e);
                return new Response("Unable to decode JSON!", Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
            } catch (SQLException e) {
                log.error("Unable to save message!", e);
                return new Response("Unable to save Message!", Status.INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
            }
        });
        routes.addCatchAll(new ResolverResponse(databaseManager, gson));

        log.info("Starting Server...");

        Server.start(routes, configManager.getPort(), configManager.getMaxSize());
    }
}
