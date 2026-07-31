package com.example.myrajourney.rehab.database.converters;

import androidx.room.TypeConverter;
import com.example.myrajourney.rehab.models.MotionFrame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Room type converter for List<MotionFrame>
 */
public class MotionDataConverter {
    
    private static final Gson gson = new Gson();
    
    @TypeConverter
    public static List<MotionFrame> fromString(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<MotionFrame>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromMotionFrameList(List<MotionFrame> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
}