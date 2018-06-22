package nl.quintor.studybits.indy.wrapper.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class IntegerEncodingUtilTest {
    @Test
    public void testEncodeDecode() throws UnsupportedEncodingException {
        testEncodeDecode(1);
        testEncodeDecode(0);
        testEncodeDecode(Integer.MAX_VALUE);
        testEncodeDecode(Integer.MIN_VALUE);
        testEncodeDecode("");
        testEncodeDecode("blah");
        testEncodeDecode("longersomething");
    }

    private void testEncodeDecode(Object input) throws UnsupportedEncodingException {
        BigInteger encode = IntegerEncodingUtil.encode(input);
        Object output = IntegerEncodingUtil.decode(encode);

        assertThat("Object does not match encode/decode contract " + input, input, is(equalTo(output)));
    }
}