package com.example.myrajourney.rehab.database.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Room type converter for List<Float>
 */
public class FloatListConverter {
    
    private static final Gson gson = new Gson();
    
    @TypeConverter
    public static List<Float> fromString(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<Float>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromFloatList(List<Float> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
}