package com.example.smartscanner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.smartscanner.models.ScanModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class HistoryManager {
    private static final String PREF_NAME = "scan_history";
    private static final String KEY_LIST = "history_list";

    public static void saveScan(Context context, ScanModel model) {
        ArrayList<ScanModel> list = getHistory(context);
        list.add(0, model); // New scan list me sab se upar aayega

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(list);

        editor.putString(KEY_LIST, json);
        editor.apply();
    }

    public static ArrayList<ScanModel> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_LIST, null);

        Type type = new TypeToken<ArrayList<ScanModel>>() {}.getType();

        if (json != null) {
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }
}