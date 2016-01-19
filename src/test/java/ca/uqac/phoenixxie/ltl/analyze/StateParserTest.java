package ca.uqac.phoenixxie.ltl.analyze;

import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;

public class StateParserTest {

    @Test
    public void testParser() throws Exception {
        State state = StateParser.parse("a > 10 && b < 0", -1000, 1000);
        State.Expr expr = state.getStateExpr();
        HashMap<String, Integer> vals = new HashMap<String, Integer>();

        vals.put("a", 100);
        vals.put("b", -50);
        assertEquals(true, expr.getResult(vals));

        vals.put("b", 20);
        assertEquals(false, expr.getResult(vals));
    }
}