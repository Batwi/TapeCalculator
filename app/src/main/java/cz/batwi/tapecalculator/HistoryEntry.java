package cz.batwi.tapecalculator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HistoryEntry {
    private final List<String> tokens;
    private final String result;

    public HistoryEntry(List<String> tokens, String result) {
        this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
        this.result = result;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public String getResult() {
        return result;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray tokenArray = new JSONArray();
        for (String token : tokens) tokenArray.put(token);
        object.put("tokens", tokenArray);
        object.put("result", result);
        return object;
    }

    public static HistoryEntry fromJson(JSONObject object) throws JSONException {
        JSONArray tokenArray = object.getJSONArray("tokens");
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < tokenArray.length(); i++) tokens.add(tokenArray.getString(i));
        return new HistoryEntry(tokens, object.getString("result"));
    }
}
