package tech.lerk.meshtalk.gateway.responses;

import sh.lrk.yahs.IResponse;
import sh.lrk.yahs.Request;

public abstract class JSONResponse implements IResponse {
    protected static String getJSONBody(Request request) {
        String[] jsonSplit = request.toString().split("\n\\{");
        return "{" + jsonSplit[jsonSplit.length - 1];
    }
}
