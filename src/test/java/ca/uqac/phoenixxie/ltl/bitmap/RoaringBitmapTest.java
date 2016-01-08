package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;

import static org.junit.Assert.*;

public class RoaringBitmapTest {
    @Test
    public void testBasic() throws Exception {
        RoaringBitmap bitmap = new RoaringBitmap();
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
    public void testOpAnd() throws Exception {
        RoaringBitmap bm1 = new RoaringBitmap();
        RoaringBitmap bm2 = new RoaringBitmap();

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
        RoaringBitmap bm1 = new RoaringBitmap();
        RoaringBitmap bm2 = new RoaringBitmap();

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
        RoaringBitmap bm1 = new RoaringBitmap();
        RoaringBitmap bm2 = new RoaringBitmap();

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

    @Test
    public void testIterator() {
        RoaringBitmap bitmap = new RoaringBitmap();
        bitmap.add(false);
        bitmap.add(true);
        bitmap.add(false);

        bitmap.addMany(true, 2);
        bitmap.addMany(false, 2);
        assertEquals("1", 7, bitmap.size());

        LTLBitmap.BitmapIterator it = bitmap.begin();
        assertEquals("2", 0, it.index());
        assertEquals("3", false, it.currentBit());
        it.moveForward(1);
        assertEquals("4", 1, it.index());
        assertEquals("5", true, it.currentBit());

        LTLBitmap.BitmapIterator it2 = it.find0();
        assertEquals("6", 2, it2.index());
        assertEquals("7", false, it2.currentBit());

        it2.moveForward(1);
        LTLBitmap.BitmapIterator it3 = it2.find1();
        assertEquals("8", 3, it3.index());

        assertEquals("9", false, it3.isEnd());
        it3.moveForward(4);
        assertEquals("10", true, it3.isEnd());

        it = bitmap.end();
        assertEquals("11", it.index(), bitmap.size());
        assertEquals("12", 4, bitmap.last1());
        assertEquals("13", 6, bitmap.last0());
    }
}