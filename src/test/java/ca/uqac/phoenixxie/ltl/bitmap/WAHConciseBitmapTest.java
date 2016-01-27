package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WAHConciseBitmapTest {
    @Test
    public void testBasic() throws Exception {
        ConciseBitmap bitmap = new ConciseBitmap(true);
        bitmap.add(false);
        bitmap.add(true);
        bitmap.add(false);

        assertEquals(3, bitmap.size());
        assertEquals(false, bitmap.get(0));
        assertEquals(true, bitmap.get(1));
        assertEquals("010", bitmap.toString());

        bitmap.addMany(true, 2);
        assertEquals(5, bitmap.size());
        assertEquals(4, bitmap.last1());
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
    public void testLastBit() throws Exception {
        ConciseBitmap bitmap = new ConciseBitmap(true);
        bitmap.addMany(true, 100);
        assertEquals("1", -1, bitmap.last0());
        assertEquals("2", 99, bitmap.last1());
        bitmap.addMany(false, 100);
        assertEquals("3", 199, bitmap.last0());
        assertEquals("4", 99, bitmap.last1());
        LTLBitmap.BitmapAdapter bm = bitmap.removeFirstBit();
        assertEquals("5", 198, bm.last0());
        assertEquals("6", 98, bm.last1());
        bm.add(true);
        assertEquals("7", 198, bm.last0());
        assertEquals("8", 199, bm.last1());
    }

    @Test
    public void testRemoveFirstBit() throws Exception {
        ConciseBitmap bitmap = new ConciseBitmap(true);
        bitmap.add(true);
        LTLBitmap.BitmapAdapter bm = bitmap.removeFirstBit();
        assertEquals("1", 0, bm.size());
        bitmap.addMany(false, 64);
        assertEquals("2", 64, bitmap.last0());
        assertEquals("3", 0, bitmap.last1());
        bm = bitmap.removeFirstBit();
        assertEquals("4", 63, bm.last0());
        assertEquals("5", -1, bm.last1());
        assertEquals("6", 64, bm.size());
        bitmap.add(true);
        assertEquals("7", 64, bitmap.last0());
        assertEquals("8", 65, bitmap.last1());
        bm = bitmap.removeFirstBit();
        assertEquals("9", 63, bm.last0());
        assertEquals("10", 64, bm.last1());
        assertEquals("11", 65, bm.size());
    }

    @Test
    public void testOpAnd() throws Exception {
        ConciseBitmap bm1 = new ConciseBitmap(true);
        ConciseBitmap bm2 = new ConciseBitmap(true);

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
        ConciseBitmap bm1 = new ConciseBitmap(true);
        ConciseBitmap bm2 = new ConciseBitmap(true);

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
        ConciseBitmap bm1 = new ConciseBitmap(true);
        ConciseBitmap bm2 = new ConciseBitmap(true);

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
        ConciseBitmap bitmap = new ConciseBitmap(true);
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