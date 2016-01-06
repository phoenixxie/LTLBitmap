package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;

import static org.junit.Assert.*;

public class RawBitmapTest {
    @Test
    public void testBasic() throws Exception {
        RawBitmap bitmap = new RawBitmap();
        bitmap.add(false);
        bitmap.add(true);
        bitmap.add(false);

        assertEquals(3, bitmap.size());
        assertEquals(false, bitmap.get(0));
        assertEquals(true, bitmap.get(1));
        assertEquals("010", bitmap.toString());

        bitmap.addMany(true, 2);
        assertEquals(4, bitmap.last1());
        assertEquals(5, bitmap.size());
        bitmap.addMany(false, 2);
        assertEquals(6, bitmap.last0());
        assertEquals(7, bitmap.size());

        assertEquals(false, bitmap.firstBit());
        LTLBitmap.BitmapAdapter bm = bitmap.removeFirstBit();
        assertEquals(true, bm.firstBit());
        assertEquals(6, bm.size());
        assertEquals("101100", bm.toString());

        bm = bm.opNot();
        assertEquals("010011", bm.toString());
    }

    @Test
    public void testIterator() throws Exception {
        RawBitmap bitmap = new RawBitmap();
        bitmap.add(false);
        bitmap.add(true);
        bitmap.add(false);

        bitmap.addMany(true, 2);
        bitmap.addMany(false, 2);
        assertEquals(bitmap.size(), 7);

        LTLBitmap.BitmapIterator it = bitmap.begin();
        assertEquals(it.index(), 0);
        assertEquals(it.currentBit(), false);
        it.moveForward(1);
        assertEquals(it.index(), 1);
        assertEquals(it.currentBit(), true);

        LTLBitmap.BitmapIterator it2 = it.find0();
        assertEquals(it2.index(), 2);
        assertEquals(it2.currentBit(), false);

        it2.moveForward(1);
        LTLBitmap.BitmapIterator it3 = it2.find0();
        assertEquals(5, it3.index());

        assertEquals(it3.isEnd(), false);
        it3.moveForward(2);
        assertEquals(it3.isEnd(), true);

        it = bitmap.end();
        assertEquals(it.index(), bitmap.size());
        assertEquals(4, bitmap.last1());
        assertEquals(6, bitmap.last0());
    }

    @Test
    public void testOpAnd() throws Exception {
        RawBitmap bm1 = new RawBitmap();
        RawBitmap bm2 = new RawBitmap();

        bm1.add(true);
        bm1.add(false);
        bm1.add(true);
        bm1.add(false);

        bm2.add(false);
        bm2.add(true);
        bm2.add(true);
        bm2.add(false);

        LTLBitmap.BitmapAdapter bm = bm1.opAnd(bm2);
        assertEquals(bm.toString(), "0010");
    }

    @Test
    public void testOpOr() throws Exception {
        RawBitmap bm1 = new RawBitmap();
        RawBitmap bm2 = new RawBitmap();

        bm1.add(true);
        bm1.add(false);
        bm1.add(true);
        bm1.add(false);

        bm2.add(false);
        bm2.add(true);
        bm2.add(true);
        bm2.add(false);

        LTLBitmap.BitmapAdapter bm = bm1.opOr(bm2);
        assertEquals(bm.toString(), "1110");
    }

    @Test
    public void testOpXor() throws Exception {
        RawBitmap bm1 = new RawBitmap();
        RawBitmap bm2 = new RawBitmap();

        bm1.add(true);
        bm1.add(false);
        bm1.add(true);
        bm1.add(false);

        bm2.add(false);
        bm2.add(true);
        bm2.add(true);
        bm2.add(false);

        LTLBitmap.BitmapAdapter bm = bm1.opXor(bm2);
        assertEquals(bm.toString(), "1100");
    }
}