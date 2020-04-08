package tech.lerk.meshtalk.gateway.responses;

import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.lrk.yahs.ContentType;
import sh.lrk.yahs.Request;
import sh.lrk.yahs.Response;
import sh.lrk.yahs.Status;
import tech.lerk.meshtalk.Utils;
import tech.lerk.meshtalk.entities.AdminCommand;
import tech.lerk.meshtalk.entities.Handshake;
import tech.lerk.meshtalk.gateway.NotImplementedException;

import java.sql.SQLException;

public class AdminResponse extends JSONResponse {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AdminResponse.class);

    private AdminResponse() {
    }


    public static Response handle(Request request) {
        String json = getJSONBody(request);
        try {
            AdminCommand command = Utils.getGson().fromJson(json, AdminCommand.class);
            log.info("Got new admin command from: '" + request.getClientAddress() + "'!");
            //TODO: handle command
            return new Response("Command received!", Status.OK, ContentType.TEXT_PLAIN);
        } catch (JsonSyntaxException e) {
            log.error("Unable to decode JSON: '" + json + "'", e);
            return new Response("Unable to decode JSON!", Status.BAD_REQUEST, ContentType.TEXT_PLAIN);
        }
    }

    @Override
    public Response getResponse(Request request) {
        throw new NotImplementedException();
    }
}
