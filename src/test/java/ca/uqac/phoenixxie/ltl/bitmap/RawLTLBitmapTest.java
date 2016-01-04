package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RawLTLBitmapTest {
    LTLBitmap left;
    LTLBitmap right;

    @Test
    public void testOpNot() throws Exception {
        left.add("10");
        left.opNot();
        assertEquals("!10", "01", left.toString());
    }

    @Test
    public void testOpAnd() throws Exception {
        left.add("1100");
        right.add("1010");
        left.opAnd(right);
        assertEquals("1100 ^ 1010", "1000", left.toString());
    }

    @Test
    public void testOpOr() throws Exception {
        left.add("1100");
        right.add("1010");
        left.opOr(right);
        assertEquals("1100 V 1010", "1110", left.toString());
    }

    @Test
    public void testOpThen() throws Exception {
        left.add("1100");
        right.add("1010");
        left.opThen(right);
        assertEquals("1100->1010", "1011", left.toString());
    }

    @Test
    public void testOpNext() throws Exception {
        left.add("1010");
        left.opNext();
        assertEquals("X1010", "010", left.toString());
    }

    @Test
    public void testOpGlobal() throws Exception {
        left.add("1111");
        left.opGlobal();
        assertEquals("G1111", "1111", left.toString());

        left.clear();
        left.add("0000");
        left.opGlobal();
        assertEquals("G0000", "0000", left.toString());

        left.clear();
        left.add("1010");
        left.opGlobal();
        assertEquals("G1010", "0000", left.toString());

        left.clear();
        left.add("0101");
        left.opGlobal();
        assertEquals("G0101", "0001", left.toString());
    }

    @Test
    public void testOpFuture() throws Exception {
        left.add("1111");
        left.opFuture();
        assertEquals("F1111", "1111", left.toString());

        left.clear();
        left.add("0000");
        left.opFuture();
        assertEquals("F0000", "0000", left.toString());

        left.clear();
        left.add("1010");
        left.opFuture();
        assertEquals("F1010", "1110", left.toString());

        left.clear();
        left.add("0101");
        left.opFuture();
        assertEquals("F0101", "1111", left.toString());
    }

    @Test
    public void testOpUntil() throws Exception {
        left.add("1111");
        right.add("0000");
        left.opUntil(right);
        assertEquals("1111U0000", "0000", left.toString());

        left.clear();
        right.clear();
        left.add("0000");
        right.add("1111");
        left.opUntil(right);
        assertEquals("0000U1111", "0000", left.toString());

        left.clear();
        right.clear();
        left.add("1010");
        right.add("0101");
        left.opUntil(right);
        assertEquals("1010U0101", "1111", left.toString());

        left.clear();
        right.clear();
        left.add("0101");
        right.add("1010");
        left.opUntil(right);
        assertEquals("0101U1010", "0110", left.toString());

        left.clear();
        right.clear();
        left.add("1000");
        right.add("01");
        left.opUntil(right);
        assertEquals("1000U01", "1100", left.toString());

        left.clear();
        right.clear();
        left.add("0101");
        right.add("0010");
        left.opUntil(right);
        assertEquals("0101U0010", "0110", left.toString());

        left.clear();
        right.clear();
        left.add("1000");
        right.add("1000");
        left.opUntil(right);
        assertEquals("1000U1000", "1000", left.toString());
    }

    @Test
    public void testOpWeakUntil() throws Exception {
        left.add("1111");
        right.add("0000");
        left.opWeakUntil(right);
        assertEquals("1111W0000", "1111", left.toString());

        left.clear();
        right.clear();
        left.add("0000");
        right.add("1111");
        left.opWeakUntil(right);
        assertEquals("0000W1111", "0000", left.toString());

        left.clear();
        right.clear();
        left.add("1010");
        right.add("0101");
        left.opWeakUntil(right);
        assertEquals("1010W0101", "1111", left.toString());

        left.clear();
        right.clear();
        left.add("0101");
        right.add("1010");
        left.opWeakUntil(right);
        assertEquals("0101W1010", "0111", left.toString());

        left.clear();
        right.clear();
        left.add("1000");
        right.add("01");
        left.opWeakUntil(right);
        assertEquals("1000W01", "1100", left.toString());

        left.clear();
        right.clear();
        left.add("0101");
        right.add("0010");
        left.opWeakUntil(right);
        assertEquals("0101W0010", "0111", left.toString());

        left.clear();
        right.clear();
        left.add("1000");
        right.add("1000");
        left.opWeakUntil(right);
        assertEquals("1000W1000", "1000", left.toString());
    }

    @Test
    public void testOpRelease() throws Exception {
        left.add("1111");
        right.add("0000");
        left.opRelease(right);
        assertEquals("1111R0000", "0000", left.toString());

        left.clear();
        right.clear();
        left.add("0000");
        right.add("1111");
        left.opRelease(right);
        assertEquals("0000R1111", "1111", left.toString());

        left.clear();
        right.clear();
        left.add("1000");
        right.add("01");
        left.opRelease(right);
        assertEquals("1000R01", "0100", left.toString());

        left.clear();
        right.clear();
        left.add("1010");
        right.add("0101");
        left.opRelease(right);
        assertEquals("1010R0101", "0101", left.toString());

        left.clear();
        right.clear();
        left.add("0101");
        right.add("1010");
        left.opRelease(right);
        assertEquals("0101R1010", "1010", left.toString());

        left.clear();
        right.clear();
        left.add("1000");
        right.add("1000");
        left.opRelease(right);
        assertEquals("1000R1000", "1000", left.toString());
    }

    @Test
    public void testEquation() {
        left.add("1010");
        right.add("0101");
        left.opUntil(right);
        left.opNot();
        String s1 = left.toString();

        left.clear();
        right.clear();
        left.add("1010");
        right.add("0101");
        left.opNot();
        right.opNot();
        left.opRelease(right);
        String s2 = left.toString();

        assertEquals("¬(φ U ψ) ≡ ¬φ R ¬ψ", s1, s2);
    }

    @Before
    public void setUp() throws Exception {
        left = new LTLBitmap(LTLBitmap.Type.RAW);
        right = new LTLBitmap(LTLBitmap.Type.RAW);
    }
}