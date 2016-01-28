package ca.uqac.phoenixxie.ltl.bitmap;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ThreadLocalRandom;

public class ConciseLTLBitmapTest extends LTLBitmapTest {

    @Override
    public LTLBitmap newBm(String in) {
        LTLBitmap bm = new LTLBitmap(LTLBitmap.Type.CONCISE);
        bm.add(in);
        return bm;
    }

    @Test
    public void test1() throws Exception {
        LTLBitmap raw1 = new LTLBitmap(LTLBitmap.Type.RAW);
        LTLBitmap raw2 = new LTLBitmap(LTLBitmap.Type.RAW);
        LTLBitmap bm1 = new LTLBitmap(LTLBitmap.Type.CONCISE);
        LTLBitmap bm2 = new LTLBitmap(LTLBitmap.Type.CONCISE);

        for (int i = 0; i < 500; ++i) {
            boolean b = ThreadLocalRandom.current().nextBoolean();
            raw1.add(b);
            bm1.add(b);
            b = ThreadLocalRandom.current().nextBoolean();
            raw2.add(b);
            bm2.add(b);

            String a1 = raw1.opWeakUntil(raw2).toString();
            String a2 = bm1.opWeakUntil(bm2).toString();

            if (!a1.equals(a2)) {
                System.out.println(raw1.toString());
                System.out.println(raw2.toString());
                System.out.println(a1.toString());
                System.out.println(a2.toString());
                assert false;
            }
        }
    }

//    @Test
//    public void test2() throws Exception {
//        LTLBitmap bm1 = new LTLBitmap(LTLBitmap.Type.CONCISE);
//        LTLBitmap bm2 = new LTLBitmap(LTLBitmap.Type.CONCISE);
//
//        for (int i = 0; i < 10000000; ++i) {
//            boolean b = ThreadLocalRandom.current().nextBoolean();
//            bm1.add(b);
//            b = ThreadLocalRandom.current().nextBoolean();
//            bm2.add(b);
//        }
//
//        System.out.println("Hi~");
//        bm1.opUntil(bm2);
//    }
}