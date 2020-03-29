package tech.lerk.meshtalk.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.*;
import tech.lerk.meshtalk.Meta;
import tech.lerk.meshtalk.Utils;
import tech.lerk.meshtalk.entities.Chat;
import tech.lerk.meshtalk.entities.Message;
import tech.lerk.meshtalk.entities.MetaInfo;
import tech.lerk.meshtalk.gateway.managers.ConfigManager;
import tech.lerk.meshtalk.gateway.managers.DatabaseManager;
import tech.lerk.meshtalk.gateway.responses.ResolverResponse;

import java.sql.SQLException;

public class Main {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static DatabaseManager databaseManager;

    private static final Gson gson = Utils.getGson();

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
        routes.add(Method.GET, "/favicon.ico", "html/favicon.ico");
        routes.add(Method.GET, "/meta", req -> {
            log.info("Serving meta to: '" + req.getClientAddress() + "'");
            MetaInfo metaInfo = new MetaInfo();
            metaInfo.setApiVersion(Meta.API_VERSION);
            metaInfo.setCoreVersion(Meta.CORE_VERSION);
            return new Response(gson.toJson(metaInfo), ContentType.APPLICATION_OCTET_STREAM);
        });
        routes.addCatchAll(new ResolverResponse(databaseManager, gson));

        log.info("Starting Server...");

        Server.start(routes, configManager.getPort(), configManager.getMaxSize());
    }
}
