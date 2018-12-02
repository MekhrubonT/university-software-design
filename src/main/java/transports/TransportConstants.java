package transports;

import org.json.simple.JSONObject;

public class TransportConstants {
    public static final String TRANSPORT_ID = "id";

    public static final String TRANSPORT_ACTION = "action";
    public static final String TRANSPORT_ACTION_LOGIN_OR_REGISTER = "login-or-register";

    public static final String TRANSPORT_ACTION_JOIN_GAME = "join-game";
    public static final Object TRANSPORT_ACTION_GAME_CREATED = "game-created";
    public static final Object TRANSPORT_COLOR = "color";
    public static final Object TRANSPORT_RESULT_COLOR_WHITE = "color-white";
    public static final Object TRANSPORT_RESULT_COLOR_BLACK = "color-black";

    public static final String TRANSPORT_ACTION_MOVE = "make-move";
    public static final String TRANSPORT_ACTION_MOVE_FROM = "move-from";
    public static final String TRANSPORT_ACTION_MOVE_TO = "move-to";

    public static final String TRANSPORT_TOKEN = "token";

    public static final String TRANSPORT_RESULT = "result";
    public static final String TRANSPORT_RESULT_OK = "result-ok";

    public static final JSONObject RESPONSE_OK;
    public static final JSONObject COLOR_WHITE;
    public static final JSONObject COLOR_BLACK;
    static {
        JSONObject responseOk = new JSONObject();
        responseOk.put(TRANSPORT_RESULT, TRANSPORT_RESULT_OK);
        RESPONSE_OK = responseOk;
    }

    static {
        JSONObject colorWhite = new JSONObject();
        colorWhite.put(TRANSPORT_ACTION, TRANSPORT_ACTION_GAME_CREATED);
        colorWhite.put(TRANSPORT_COLOR, TRANSPORT_RESULT_COLOR_WHITE);
        COLOR_WHITE = colorWhite;
    }

    static {
        JSONObject colorBlack = new JSONObject();
        colorBlack.put(TRANSPORT_ACTION, TRANSPORT_ACTION_GAME_CREATED);
        colorBlack.put(TRANSPORT_COLOR, TRANSPORT_RESULT_COLOR_BLACK);
        COLOR_BLACK = colorBlack;
    }
}
