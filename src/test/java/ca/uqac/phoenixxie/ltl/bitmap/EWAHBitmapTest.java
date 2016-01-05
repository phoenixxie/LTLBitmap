package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EWAHBitmapTest {
    @Test
    public void testBasic() throws Exception {
        EWAHBitmap bitmap = new EWAHBitmap();
        bitmap.add(false);
        bitmap.add(true);
        bitmap.add(false);

        assertEquals(3, bitmap.size());
        assertEquals(false, bitmap.get(0));
        assertEquals(true, bitmap.get(1));
        assertEquals("010", bitmap.toString());

        bitmap.addMany(true, 2);
        assertEquals(true, bitmap.lastBit());
        assertEquals(5, bitmap.size());
        bitmap.addMany(false, 2);
        assertEquals(false, bitmap.lastBit());
        assertEquals(7, bitmap.size());
        assertEquals(false, bitmap.firstBit());
        assertEquals("0101100", bitmap.toString());

        LTLBitmap.BitmapAdapter bm = bitmap.opNot();
        assertEquals(bm.toString(), "1010011");

//        LTLBitmap.BitmapAdapter bm = bitmap.removeFirstBit();
//        assertEquals(bm.firstBit(), true);
//        assertEquals(bm.size(), 6);
//        assertEquals(bm.toString(), "101100");
//

    }

    @Test
    public void testFirstBit() throws Exception {

    }

    @Test
    public void testLastBit() throws Exception {

    }

    @Test
    public void testOpNot() throws Exception {

    }

    @Test
    public void testOpAnd() throws Exception {

    }

    @Test
    public void testOpOr() throws Exception {

    }

    @Test
    public void testOpXor() throws Exception {

    }

    @Test
    public void testRemoveFirstBit() throws Exception {

    }

    @Test
    public void testRemoveFromEnd() throws Exception {

    }

}