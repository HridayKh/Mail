package utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import models.Attachment;

public class EmailRequestUtil {

    public static List<String> jsonArrayToStringList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            String s = arr.optString(i, null);
            if (s != null) list.add(s);
        }
        return list;
    }

    public static List<Attachment> jsonArrayToAttachmentList(JSONArray arr) {
        List<Attachment> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;
            Attachment a = new Attachment();
            a.setFilename(obj.optString("filename", null));
            a.setUrl(obj.optString("url", null)); // Support for remote URL
            list.add(a);
        }
        return list;
    }
}