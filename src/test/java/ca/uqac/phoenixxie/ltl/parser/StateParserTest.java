package ca.uqac.phoenixxie.ltl.parser;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class StateParserTest {
    @Test
    public void testParser() throws Exception {
        StateParser.Result result = StateParser.parseState("a > 9 && b < 10");
        StateParser.Expr expr = result.getStateExpr();
        HashMap<String, Integer> vals = new HashMap<String, Integer>();

        vals.put("a", 100);
        vals.put("b", -50);
        assertEquals(true, expr.getResult(vals));

        vals.put("b", 20);
        assertEquals(false, expr.getResult(vals));
    }
}