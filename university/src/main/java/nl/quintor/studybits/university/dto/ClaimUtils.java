package nl.quintor.studybits.university.dto;

import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.dto.SchemaDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClaimUtils {

    public static SchemaDefinition getSchemaDefinition(Class<?> claimType) {
        validateNamedVersionType(claimType);
        VersionInfo versionInfo = claimType.getAnnotation(VersionInfo.class);
        List<String> fieldNames = getFieldNames(claimType);
        return new SchemaDefinition(versionInfo.name(), versionInfo.version(), fieldNames);
    }

    public static Version getVersion(Class<?> versionedType) {
        validateNamedVersionType(versionedType);
        VersionInfo versionInfo = versionedType.getAnnotation(VersionInfo.class);
        return new Version(versionInfo.name(), versionInfo.version());
    }

    public static Map<String, Object> getMapOfClaim(Object claim) {
        validateNamedVersionType(claim.getClass());
        return Arrays.stream(claim.getClass()
                .getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, f -> getClaimProperty(claim, f)));
    }

    private static List<String> getFieldNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private static void validateNamedVersionType(Class<?> clazz) {
        Validate.isTrue(clazz.isAnnotationPresent(VersionInfo.class), "Given type must be annotated with @VersionInfo.");
    }

    @SneakyThrows
    private static Object getClaimProperty(Object claim, Field field) {
        field.setAccessible(true);
        Object value = field.get(claim);
        if (isValidClaimProperty(value)) {
            return value;
        }
        throw new IllegalStateException(String.format("Claim property '%s' is invalid.", field.getName()));
    }

    private static boolean isValidClaimProperty(Object o) {
        return o instanceof Integer || o instanceof String;
    }


    public static List<ProofAttribute> getProofAttributes(Class<?> proofClass) {
        return Arrays
                .stream(proofClass.getDeclaredFields())
                .map(ClaimUtils::getProofAttribute)
                .collect(Collectors.toList());
    }


    public static ProofAttribute getProofAttribute(Field field) {
        ProofAttributeInfo proofAttributeInfo = field.getAnnotation(ProofAttributeInfo.class);
        if(proofAttributeInfo != null) {
            String attributeName = StringUtils.isNotEmpty(proofAttributeInfo.attributeName()) ? proofAttributeInfo.attributeName() : field.getName();
            List<Version> versions = Arrays
                    .stream(proofAttributeInfo.schemas())
                    .map(claimSchema -> new Version(claimSchema.name(), claimSchema.version()))
                    .collect(Collectors.toList());
            return new ProofAttribute(field, attributeName, versions);
        }
        return new ProofAttribute(field);
    }

}