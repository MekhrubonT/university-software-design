package transports;

import org.json.simple.JSONObject;

public class TransportConstants {
    public static final String TRANSPORT_ID = "id";

    public static final String TRANSPORT_ACTION = "action";
    public static final String TRANSPORT_ACTION_REGISTER = "register";
    public static final String TRANSPORT_ACTION_LOGIN = "login";

    public static final String TRANSPORT_ACTION_JOIN_GAME = "join-game";
    public static final Object TRANSPORT_ACTION_GAME_CREATED = "game-created";
    public static final Object TRANSPORT_COLOR = "color";
    public static final Object TRANSPORT_RESULT_COLOR_WHITE = "color-white";
    public static final Object TRANSPORT_RESULT_COLOR_BLACK = "color-black";

    public static final String TRANSPORT_ACTION_MOVE = "make-move";
    public static final String TRANSPORT_ACTION_MOVE_FROM = "move-from";
    public static final String TRANSPORT_ACTION_MOVE_TO = "move-to";

    public static final String TRANSPORT_LOGIN = "login";
    public static final String TRANSPORT_PASSWORD = "password";

    public static final String TRANSPORT_PLAYER_LOGIN = "player-login";
    public static final String TRANSPORT_PLAYER_PASSWORD = "player-password";
    public static final String TRANSPORT_PLAYER_RATING = "rating";
    public static final String TRANSPORT_PLAYER_WINS = "wins";
    public static final String TRANSPORT_PLAYER_DRAWS = "draws";
    public static final String TRANSPORT_PLAYER_LOSES = "loses";

    public static final String TRANSPORT_RESULT = "result";
    public static final String TRANSPORT_RESULT_OK = "result-ok";
    public static final String TRANSPORT_RESULT_BAD = "result-bad";
    public static final String TRANSPORT_RESULT_CHECKMATE = "result-checkmate";
    public static final String TRANSPORT_RESULT_STALEMATE = "result-stalemate";

    public static final JSONObject RESPONSE_OK;
    public static final JSONObject RESPONSE_BAD;
    public static final JSONObject RESPONSE_CHECKMATE;
    public static final JSONObject RESPONSE_STALEMATE;
    public static final JSONObject COLOR_WHITE;
    public static final JSONObject COLOR_BLACK;
    public static final JSONObject JOIN_GAME_REQUEST;

    static {
        JSONObject responseOk = new JSONObject();
        responseOk.put(TRANSPORT_RESULT, TRANSPORT_RESULT_OK);
        RESPONSE_OK = responseOk;
    }
    static {
        JSONObject responseBad = new JSONObject();
        responseBad.put(TRANSPORT_RESULT, TRANSPORT_RESULT_BAD);
        RESPONSE_BAD = responseBad;
    }
    static {
        JSONObject responeCheckMate = new JSONObject();
        responeCheckMate.put(TRANSPORT_RESULT, TRANSPORT_RESULT_CHECKMATE);
        RESPONSE_CHECKMATE = responeCheckMate;
    }
    static {
        JSONObject responeStaleMate = new JSONObject();
        responeStaleMate.put(TRANSPORT_RESULT, TRANSPORT_RESULT_STALEMATE);
        RESPONSE_STALEMATE = responeStaleMate;
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
    static {
        JSONObject joinGameRequest = new JSONObject();
        joinGameRequest.put(TRANSPORT_ACTION, TRANSPORT_ACTION_JOIN_GAME);
        JOIN_GAME_REQUEST = joinGameRequest;
    }
}
