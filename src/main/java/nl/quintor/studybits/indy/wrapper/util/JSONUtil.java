package nl.quintor.studybits.indy.wrapper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;

public class JSONUtil {
    public static final ObjectMapper mapper = new ObjectMapper();

    private JSONUtil() {
    }

    static {
        mapper.registerModule(new Jdk8Module());
    }

    public static<T> T readObjectByPointer(String json, String pointer, Class<T> valueType) throws IOException {
        return mapper.treeToValue(mapper.readTree(json).at(pointer), valueType);
    }
}
