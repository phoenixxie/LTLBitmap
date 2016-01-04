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

        assertEquals(bitmap.size(), 3);
        assertEquals(bitmap.get(0), false);
        assertEquals(bitmap.get(1), true);
        assertEquals(bitmap.toString(), "010");

        bitmap.addMany(true, 2);
        assertEquals(bitmap.lastBit(), true);
        assertEquals(bitmap.size(), 5);
        bitmap.addMany(false, 2);
        assertEquals(bitmap.lastBit(), false);
        assertEquals(bitmap.size(), 7);

        assertEquals(bitmap.firstBit(), false);
        LTLBitmap.BitmapAdapter bm = bitmap.removeFirstBit();
        assertEquals(bm.firstBit(), true);
        assertEquals(bm.size(), 6);
        assertEquals(bm.toString(), "101100");

        bm = bm.opNot();
        assertEquals(bm.toString(), "010011");
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
        it2 = it.rfind1();
        assertEquals(4, it2.index());
        it2 = it.rfind0();
        assertEquals(6, it2.index());
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