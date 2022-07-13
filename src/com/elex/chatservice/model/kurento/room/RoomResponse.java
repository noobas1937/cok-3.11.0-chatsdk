package com.elex.chatservice.model.kurento.room;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


import com.elex.chatservice.model.kurento.room.KurentoRoomAPI.Method;

/**
 * Room response class
 */
public class RoomResponse {

    private int id = 0;
    private String sessionId = null;
    private List<HashMap<String, String>> values = null;
    private HashMap<String, Boolean> users = null;
    private String sdpAnswer = null;
    private Method method;

    public RoomResponse(String id, JSONObject obj){
        this.id = Integer.valueOf(id);
        this.sessionId = this.getJSONObjectSessionId(obj);
        this.values = this.getJSONObjectValues(obj);
    }

    @SuppressWarnings("unused")
    public List<HashMap<String, String>> getValues() {
        return values;
    }

    @SuppressWarnings("unused")
    public int getId() {
        return this.id;
    }

    @SuppressWarnings("unused")
    public Map<String, Boolean> getUsers() {
        return this.users;
    }

    @SuppressWarnings("unused")
    public String getSdpAnswer() {
        return this.sdpAnswer;
    }

    @SuppressWarnings("unused")
    public String getSessionId() {
        return sessionId;
    }

    @SuppressWarnings("unused")
    public Method getMethod() {
        return method;
    }

    @SuppressWarnings("unused")
    public List<String> getValue(String key){
        List<String> result = new Vector<String>();
        for (HashMap<String, String> aMap : values) {
            result.add(aMap.get(key));
        }
        return result;
    }

    public String toString(){
        return "RoomResponse: "+id+" - "+sessionId+" - "+valuesToString();
    }

    private String getJSONObjectSessionId(JSONObject obj){
        if(obj.containsKey("sessionId")) {
            return obj.get("sessionId").toString();
        } else {
            return null;
        }
    }

    private List<HashMap<String, String>> getJSONObjectValues(JSONObject obj){
        List<HashMap<String, String>> result = new Vector<HashMap<String,String>>();

        // Try to find value field. Value is specific to room join response
        // and contains a list of all existing users
        if(obj.containsKey("value")) {
            JSONArray valueArray = (JSONArray)obj.get("value");
            method = Method.JOIN_ROOM;
            users = new HashMap<String, Boolean>();
            // Iterate through the user array. The user array contains a list of
            // dictionaries, each dict containing field "id" and "streams"
            // where "id" is the username and "streams" a list of dictionary.
            // Each stream dictionary contains "id" key and the type of the
            // stream as the value, which is currently aways "webcam"
            for(int i=0; i<valueArray.size(); i++) {
                HashMap<String, String> vArrayElement = new HashMap<String, String>();
                JSONObject jo = (JSONObject) valueArray.get(i);
                Set<String> keys = jo.keySet();
                for(String key : keys){
                    vArrayElement.put(key, jo.get(key).toString());

                }
                result.add(vArrayElement);

                // Fill in the users dictionary
                if (jo.containsKey("id")) {
                    String username = jo.get("id").toString();
                    Boolean webcamPublished;
                    // If the array entry contains both id and streams then from the
                    // current implementation we already know that the webcam stream has
                    // been published
                    webcamPublished = jo.containsKey("streams");
                    users.put(username, webcamPublished);
                }
            }
        }

        if (obj.containsKey("sdpAnswer")){
            sdpAnswer = (String)obj.get("sdpAnswer");
            HashMap<String, String> vArrayElement = new HashMap<String, String>();

            vArrayElement.put("sdpAnswer", sdpAnswer);
            result.add(vArrayElement);
        }
        if (result.isEmpty()){
            result = null;
        }

        return result;
    }

    private String valuesToString(){
        StringBuilder sb = new StringBuilder();
        if(this.values!=null){
            for (HashMap<String, String> aValueMap : values ) {
                sb.append("{");
                for (Map.Entry<String, String> entry : aValueMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    sb.append(key).append("=").append(value).append(", ");
                }
                sb.append("},");
            }
            return sb.toString();
        } else return null;
    }
}