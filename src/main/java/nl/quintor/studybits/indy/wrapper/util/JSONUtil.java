package nl.quintor.studybits.indy.wrapper.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JSONUtil {
    public static ObjectMapper mapper = new ObjectMapper();

    public static<T> T readObjectByPointer(String json, String pointer, Class<T> valueType) throws IOException {
        return mapper.treeToValue(mapper.readTree(json).at(pointer), valueType);
    }
}
