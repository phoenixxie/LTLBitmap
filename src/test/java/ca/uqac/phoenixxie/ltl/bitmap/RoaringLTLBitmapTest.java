package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RoaringLTLBitmapTest extends LTLBitmapTest {

    @Override
    public LTLBitmap newBm(String in) {
        LTLBitmap bm = new LTLBitmap(LTLBitmap.Type.ROARING);
        bm.add(in);
        return bm;
    }
}