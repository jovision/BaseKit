package com.jovision.basekit;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ZHP on 2017/6/24.
 */
public class JsonUtils {

    private static final String TAG = "JsonUtils";
    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String str, Class<T> type) {

        try{
            Gson gson = new Gson();
            LogUtil.v(TAG,"fromJson="+str);
            T t = gson.fromJson(str, type);
            return t;
        } catch (Exception e){
            LogUtil.e(TAG,"Exception-fromJson="+str);
            e.printStackTrace();
        }

        return null;
    }


    public static JSONObject map2Json(Map<?, ?> data) {
        JSONObject object = new JSONObject();

        for (Map.Entry<?, ?> entry : data.entrySet()) {
            String key = (String) entry.getKey();
            if (key == null) {
                throw new NullPointerException("key == null");
            }
            try {
                object.put(key, wrap(entry.getValue()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return object;
    }

    public static JSONArray collection2Json(Collection<?> data) {
        JSONArray jsonArray = new JSONArray();
        if (data != null) {
            for (Object aData : data) {
                jsonArray.put(wrap(aData));
            }
        }
        return jsonArray;
    }

    public static JSONArray object2Json(Object data) throws JSONException {
        if (!data.getClass().isArray()) {
            throw new JSONException("Not a primitive data: " + data.getClass());
        }
        final int length = Array.getLength(data);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < length; ++i) {
            jsonArray.put(wrap(Array.get(data, i)));
        }

        return jsonArray;
    }

    private static Object wrap(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return collection2Json((Collection<?>) o);
            } else if (o.getClass().isArray()) {
                return object2Json(o);
            }
            if (o instanceof Map) {
                return map2Json((Map<?, ?>) o);
            }

            if (o instanceof Boolean || o instanceof Byte || o instanceof Character || o instanceof Double || o instanceof Float || o instanceof Integer || o instanceof Long
                    || o instanceof Short || o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static JSONObject string2JSONObject(String json) {
        JSONObject jsonObject = null;
        try {
            JSONTokener jsonParser = new JSONTokener(json);
            jsonObject = (JSONObject) jsonParser.nextValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static <T> List<T> jsonArrayToList(JSONArray array, Class<T> clazz){
        List<T> list = new ArrayList<>();
        for (int i=0;i<array.length();i++) {
            try{
                JSONObject json = array.getJSONObject(i);
                list.add(new Gson().fromJson(json+"", clazz));
            }catch (Exception e){
               e.printStackTrace();
            }
        }
        return list;
    }


    /**
     * 把对象转成json字符串
     *
     * @param t
     * @return
     */
    public static <T> String parserObjectToGson(T t) {
        return (new Gson()).toJson(t);
    }

    /**
     * 根据指定key提取json字符串
     *
     * @param json
     * @param key
     * @return String or "";
     */
    public static String getGsonValueByKey(String json, String key) {
        String value = "";
        try {
            value = (new JSONObject(json)).getString(key);
        } catch (Exception e) {
            return "";
        }
        return value;
    }

    /**
     * 解析字符串,返回对象
     *
     * @param <T>
     * @param json
     * @param classOft
     * @return Object
     */
    public static <T> T parserGsonToObject(String json, Class<T> classOft) {
        return (new Gson()).fromJson(json, classOft);
    }

    /**
     *
     * 解析字符串,返回list对象
     *
     *
     *
     * @param json
     *
     * @param typeToken     new TypeToken<List<T>>(){}
     *
     * @return List<T> or null;
     */
    public static <T> ArrayList<T> parserGsonToArray(String json, TypeToken<ArrayList<T>> typeToken) {
        return (new Gson()).fromJson(json, typeToken.getType());
    }

    /**
     * 解析字符串,返回map对象
     *
     * @param json
     * @param typeToken
     * @return
     */
    public static <T, V> HashMap<T, V> parserGsonToMap(String json,
                                                       TypeToken<HashMap<T, V>> typeToken) {
        return (new Gson()).fromJson(json, typeToken.getType());
    }

}
