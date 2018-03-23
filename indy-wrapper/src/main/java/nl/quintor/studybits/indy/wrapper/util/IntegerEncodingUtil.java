package nl.quintor.studybits.indy.wrapper.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class IntegerEncodingUtil {
    private static final BigInteger STRING_RANGE_START = BigInteger.valueOf(0xffffffffL);
    public static BigInteger encode(Object o) throws UnsupportedEncodingException {
        if (o instanceof Integer) {
            return BigInteger.valueOf((Integer) o);
        }

        if (o instanceof String) {
            BigInteger bigInteger = new BigInteger(((String) o).getBytes("utf8"));
            return bigInteger.add(STRING_RANGE_START);
        }

        throw new IllegalArgumentException("Object must be String or Integer");
    }

    public static Object decode(BigInteger encoding) {
        if (encoding.compareTo(STRING_RANGE_START) < 0) {
            return encoding.intValue();
        }
        else {
            return new String(encoding.subtract(STRING_RANGE_START).toByteArray(), Charset.forName("utf8"));
        }
    }

    public static boolean validateProofEncoding( List<String> value ) {
        if ( value.size() != 3 ) {
            return false;
        }

        String plainValue = value.get(1);
        String encoding = value.get(2);

        return plainValue.equals(decode(new BigInteger(encoding)));
    }

    public static JsonNode claimValuesFromMap(Map<String, Object> valueMap) throws UnsupportedEncodingException {
        ObjectNode result = JSONUtil.mapper.createObjectNode();

        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            ArrayNode value = JSONUtil.mapper.createArrayNode();

            value.add(entry.getValue().toString());
            value.add(encode(entry.getValue()).toString());
            result.set(entry.getKey(), value);
        }

        return result;
    }
}
