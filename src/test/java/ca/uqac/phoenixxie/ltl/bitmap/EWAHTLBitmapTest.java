package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EWAHTLBitmapTest {
    @Test
    public void testOpNot() throws Exception {
        LTLBitmap left = newBm("10");
        LTLBitmap bm = left.opNot();
        assertEquals("!10", "01", bm.toString());
    }

    @Test
    public void testOpAnd() throws Exception {
        LTLBitmap left = newBm("1100");
        LTLBitmap right = newBm("1010");
        LTLBitmap bm = left.opAnd(right);
        assertEquals("1100 ^ 1010", "1000", bm.toString());
    }

    @Test
    public void testOpOr() throws Exception {
        LTLBitmap left = newBm("1100");
        LTLBitmap right = newBm("1010");
        LTLBitmap bm = left.opOr(right);
        assertEquals("1100 V 1010", "1110", bm.toString());
    }

    @Test
    public void testOpThen() throws Exception {
        LTLBitmap left = newBm("1100");
        LTLBitmap right = newBm("1010");
        LTLBitmap bm = left.opThen(right);
        assertEquals("1100->1010", "1011", bm.toString());
    }

    @Test
    public void testOpNext() throws Exception {
        LTLBitmap left = newBm("1010");
        LTLBitmap bm = left.opNext();
        assertEquals("X1010", "010", bm.toString());
    }

    @Test
    public void testOpGlobal() throws Exception {
        LTLBitmap left = newBm("1111");
        LTLBitmap bm = left.opGlobal();
        assertEquals("G1111", "1111", bm.toString());

        left = newBm("0000");
        bm = left.opGlobal();
        assertEquals("G0000", "0000", bm.toString());

        left = newBm("1010");
        bm = left.opGlobal();
        assertEquals("G1010", "0000", bm.toString());

        left = newBm("0101");
        bm = left.opGlobal();
        assertEquals("G0101", "0001", bm.toString());
    }

    @Test
    public void testOpFuture() throws Exception {
        LTLBitmap left = newBm("1111");
        LTLBitmap bm = left.opFuture();
        assertEquals("F1111", "1111", bm.toString());

        left = newBm("0000");
        bm = left.opFuture();
        assertEquals("F0000", "0000", bm.toString());

        left = newBm("1010");
        bm = left.opFuture();
        assertEquals("F1010", "1110", bm.toString());

        left = newBm("0101");
        bm = left.opFuture();
        assertEquals("F0101", "1111", bm.toString());
    }

    @Test
    public void testOpUntil() throws Exception {
        LTLBitmap left = newBm("1111");
        LTLBitmap right = newBm("0000");
        LTLBitmap bm = left.opUntil(right);
        assertEquals("1111U0000", "0000", bm.toString());

        left = newBm("0000");
        right = newBm("1111");
        bm = left.opUntil(right);
        assertEquals("0000U1111", "0000", bm.toString());

        left = newBm("1010");
        right = newBm("0101");
        bm = left.opUntil(right);
        assertEquals("1010U0101", "1111", bm.toString());

        left = newBm("0101");
        right = newBm("1010");
        bm = left.opUntil(right);
        assertEquals("0101U1010", "0110", bm.toString());

        left = newBm("1000");
        right = newBm("01");
        bm = left.opUntil(right);
        assertEquals("1000U01", "1111", bm.toString());

        left = newBm("0101");
        right = newBm("0010");
        bm = left.opUntil(right);
        assertEquals("0101U0010", "0110", bm.toString());

        left = newBm("1000");
        right = newBm("1000");
        bm = left.opUntil(right);
        assertEquals("1000U1000", "1000", bm.toString());

        left = newBm("10");
        right = newBm("0110");
        bm = left.opUntil(right);
        assertEquals("10U0110", "1110", bm.toString());
    }

    @Test
    public void testOpWeakUntil() throws Exception {
        LTLBitmap left = newBm("1111");
        LTLBitmap right = newBm("0000");
        LTLBitmap bm = left.opWeakUntil(right);
        assertEquals("1111W0000", "1111", bm.toString());

        left = newBm("0000");
        right = newBm("1111");
        bm = left.opWeakUntil(right);
        assertEquals("0000W1111", "0000", bm.toString());

        left = newBm("1011");
        right = newBm("0100");
        bm = left.opWeakUntil(right);
        assertEquals("1011W0100", "1111", bm.toString());

        left = newBm("1001");
        right = newBm("0100");
        bm = left.opWeakUntil(right);
        assertEquals("1001W0100", "1101", bm.toString());

        left = newBm("1000101");
        right = newBm("0100000");
        bm = left.opWeakUntil(right);
        assertEquals("1000101W0100", "1100001", bm.toString());

        left = newBm("1010");
        right = newBm("0101");
        bm = left.opWeakUntil(right);
        assertEquals("1010W0101", "1111", bm.toString());

        left = newBm("0101");
        right = newBm("1010");
        bm = left.opWeakUntil(right);
        assertEquals("0101W1010", "0111", bm.toString());

        left = newBm("1010");
        right = newBm("010");
        bm = left.opWeakUntil(right);
        assertEquals("1010W010", "1100", bm.toString());

        left = newBm("0101");
        right = newBm("0010");
        bm = left.opWeakUntil(right);
        assertEquals("0101W0010", "0111", bm.toString());

        left = newBm("1000");
        right = newBm("1000");
        bm = left.opWeakUntil(right);
        assertEquals("1000W1000", "1000", bm.toString());
    }

    @Test
    public void testOpRelease() throws Exception {
        LTLBitmap left = newBm("1111");
        LTLBitmap right = newBm("0000");
        LTLBitmap bm = left.opRelease(right);
        assertEquals("1111R0000", "0000", bm.toString());

        left = newBm("0000");
        right = newBm("1111");
        bm = left.opRelease(right);
        assertEquals("0000R1111", "1111", bm.toString());

        left = newBm("0110");
        right = newBm("1100");
        bm = left.opRelease(right);
        assertEquals("0110R1100", "1110", bm.toString());

        left = newBm("1101");
        right = newBm("1000");
        bm = left.opRelease(right);
        assertEquals("1101R1000", "1100", bm.toString());

        left = newBm("11100");
        right = newBm("10001");
        bm = left.opRelease(right);
        assertEquals("11100R10001", "11101", bm.toString());

        left = newBm("0000");
        right = newBm("1111");
        bm = left.opRelease(right);
        assertEquals("0000R1111", "1111", bm.toString());

        left = newBm("1000");
        right = newBm("01");
        bm = left.opRelease(right);
        assertEquals("1000R01", "0111", bm.toString());

        left = newBm("1010");
        right = newBm("0101");
        bm = left.opRelease(right);
        assertEquals("1010R0101", "0001", bm.toString());

        left = newBm("0101");
        right = newBm("1010");
        bm = left.opRelease(right);
        assertEquals("0101R1010", "0000", bm.toString());

        left = newBm("1000");
        right = newBm("1000");
        bm = left.opRelease(right);
        assertEquals("1000R1000", "1000", bm.toString());
    }

    @Test
    public void testEquivalence() {
//        LTLBitmap left = newBm("1010");
//        right = newBm("0101");
//        left.opUntil(right);
//        left.opNot();
//        String s1 = bm.toString();
//
//
//
//        LTLBitmap left = newBm("1010");
//        right = newBm("0101");
//        left.opNot();
//        right.opNot();
//        left.opRelease(right);
//        String s2 = bm.toString();
//
//        assertEquals("¬(φ U ψ) ≡ ¬φ R ¬ψ", s1, s2);
    }

    public LTLBitmap newBm(String in) {
        LTLBitmap bm = new LTLBitmap(LTLBitmap.Type.EWAH);
        bm.add(in);
        return bm;
    }
}