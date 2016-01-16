package ca.uqac.phoenixxie.ltl.parser;

import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;
import org.junit.Test;

import static org.junit.Assert.*;

public class LTLParserTest {

    @Test
    public void testParser() throws Exception {
        LTLBitmap.Type type = LTLBitmap.Type.EWAH;
        LTLBitmap[] states = new LTLBitmap[]{
                new LTLBitmap(type, "01010"),
                new LTLBitmap(type, "10101"),
                new LTLBitmap(type, "00001"),
        };

        String form = "s0 -> X s1";
        LTLParser.Result result = LTLParser.parse(form);
        LTLBitmap bm = result.ltlExpr.getResult(states);

        System.out.println(bm.toString());
    }
}