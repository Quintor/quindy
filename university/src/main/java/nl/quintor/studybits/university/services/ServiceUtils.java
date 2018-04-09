package nl.quintor.studybits.university.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;
import nl.quintor.studybits.university.entities.ClaimSchema;

public final class ServiceUtils {

    public static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new Jdk8Module());
    }

    @SneakyThrows
    public static String objectToJson(Object object) {
        return mapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static <T> T jsonToObject(String content, Class<T> objectType) {
        return mapper.readValue(content, objectType);
    }

    public static SchemaKey convertToSchemaKey(ClaimSchema claimSchema) {
        return new SchemaKey(claimSchema.getSchemaName(), claimSchema.getSchemaVersion(), claimSchema.getSchemaIssuerDid());
    }
}