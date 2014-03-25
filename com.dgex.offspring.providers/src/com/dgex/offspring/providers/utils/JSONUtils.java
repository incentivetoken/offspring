package com.dgex.offspring.providers.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtils {

  public static double getDouble(JSONObject map, String key) {
    if (map != null && map.containsKey(key)) {
      Object value = map.get(key);
      if (value instanceof Double)
        return ((Double) value).doubleValue();
      else if (value instanceof Long)
        return ((Long) value).doubleValue();
    }
    return 0;
  }

  public static String getString(JSONObject map, String key) {
    if (map != null && map.containsKey(key)) {
      Object value = map.get(key);
      if (value instanceof String)
        return (String) value;
    }
    return "";
  }

  public static JSONObject getMap(JSONObject map, String key) {
    if (map != null && map.containsKey(key)) {
      Object value = map.get(key);
      if (value instanceof JSONObject)
        return (JSONObject) value;
    }
    return null;
  }

  public static JSONArray getList(JSONObject map, String key) {
    if (map != null && map.containsKey(key)) {
      Object value = map.get(key);
      if (value instanceof JSONArray)
        return (JSONArray) value;
    }
    return null;
  }

  public static long getLong(JSONObject map, String key) {
    if (map != null && map.containsKey(key)) {
      Object value = map.get(key);
      if (value instanceof Double)
        return ((Double) value).longValue();
      else if (value instanceof Long)
        return ((Long) value).longValue();
    }
    return 0;
  }
}
