package util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class ClientExclusionStrategy implements ExclusionStrategy
{
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getName().equals("relationships")
                && f.getDeclaringClass().equals(org.smartregister.clientandeventmodel.Client.class)
                && f.getDeclaredType().equals(new TypeToken<Map<String, List<String>>>() {}.getType());
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}