package nl.quintor.studybits.indy.wrapper.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class IntegerEncodingUtil {
    public static BigInteger encode(Object o) throws UnsupportedEncodingException {
        if (o instanceof Integer) {
            return BigInteger.valueOf((Integer) o);
        }

        if (o instanceof String) {
            BigInteger bigInteger = new BigInteger(((String) o).getBytes("utf8"));
            return bigInteger.add(BigInteger.valueOf(0xffffffffL));
        }

        throw new IllegalArgumentException("Object must be String or Integer");
    }
}
