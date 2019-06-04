package nl.quintor.studybits.indy.wrapper.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.dto.Proof;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class IntegerEncodingUtil {
    private static final byte INT_MARKER = (byte) 0x01;
    private static final byte STRING_MARKER = (byte) 0x02;


    public static BigInteger encode(Object o) throws UnsupportedEncodingException {
        if (o instanceof Integer) {
            byte[] intBytes = BigInteger.valueOf((Integer) o).toByteArray();
            byte[] encoding = prepended(INT_MARKER, intBytes);
            return new BigInteger(encoding);
        }

        if (o instanceof String) {
            byte[] stringBytes = ((String) o).getBytes("utf8");

            byte[] encoding = prepended(STRING_MARKER, stringBytes);

            return new BigInteger(encoding);
        }

        throw new IllegalArgumentException("Object must be String or Integer");
    }

    public static Object decode(BigInteger encoding) {
        byte[] bytesEncoding = encoding.toByteArray();
        byte[] bytesMarkerStripped = new byte[bytesEncoding.length-1];
        System.arraycopy(bytesEncoding, 1, bytesMarkerStripped, 0, bytesEncoding.length-1);

        switch (bytesEncoding[0]) {
            case INT_MARKER:
                return new BigInteger(bytesMarkerStripped).intValue();
            case STRING_MARKER:
                return new String(bytesMarkerStripped, Charset.forName("utf8"));
                default: throw new IllegalArgumentException("Marker byte invalid");
        }
    }

    public static boolean validateProofEncoding( Proof.RevealedValue value ) {
        String plainValue = value.getRaw();
        String encoding = value.getEncoded();
        Object decoded = decode(new BigInteger(encoding));

        if (decoded instanceof Integer) {
            return (Integer) decoded == Integer.parseInt(plainValue);
        }

        return plainValue.equals(decoded);
    }

    public static JsonNode credentialValuesFromMap(Map<String, Object> valueMap) throws UnsupportedEncodingException {
        ObjectNode result = JSONUtil.mapper.createObjectNode();

        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            ArrayNode value = JSONUtil.mapper.createArrayNode();

            value.add(entry.getValue().toString());
            value.add(encode(entry.getValue()).toString());
            result.set(entry.getKey(), value);
        }

        return result;
    }

    private static byte[] prepended(byte prepend, byte[] body) {
        byte[] result = new byte[body.length+1];
        System.arraycopy(body, 0, result, 1, body.length);

        result[0] = prepend;
        return result;
    }
}
