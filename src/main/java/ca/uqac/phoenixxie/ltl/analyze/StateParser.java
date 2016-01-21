package ca.uqac.phoenixxie.ltl.analyze;

import ca.uqac.phoenixxie.ltl.antlr4.*;
import org.antlr.v4.runtime.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

public class StateParser {

    private static class Vistor extends StateExprBaseVisitor<State.Expr> {
        private HashMap<String, HashSet<Integer>> vars = new HashMap<>();

        public HashMap<String, Integer[]> getVars() {
            HashMap<String, Integer[]> ret = new HashMap<>();
            for(HashMap.Entry<String, HashSet<Integer>> entry : vars.entrySet()) {
                Integer[] arr = entry.getValue().toArray(new Integer[entry.getValue().size()]);
                Arrays.sort(arr);
                ret.put(entry.getKey(), arr);
            }
            return ret;
        }

        @Override
        public State.Expr visitProg(StateExprParser.ProgContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public State.Expr visitNoExpr(StateExprParser.NoExprContext ctx) {
            return new State.LogicNoExpr(visit(ctx.expr()));
        }

        @Override
        public State.Expr visitCompExpr(StateExprParser.CompExprContext ctx) {
            State.CompareOperator op;
            String name = ctx.VAR().getText();
            HashSet<Integer> intset;
            if (!vars.containsKey(name)) {
                intset = new HashSet<>();
                vars.put(name, intset);
            } else {
                intset = vars.get(name);
            }
            int num = Integer.parseInt(ctx.NUMBER().getText());

            switch (ctx.compOp.getType()) {
                case StateExprParser.EQ:
                    op = State.CompareOperator.EQ;
                    // (-inf, num), num, [num + 1, +inf)
                    intset.add(num);
                    intset.add(num + 1);
                    break;
                case StateExprParser.NEQ:
                    op = State.CompareOperator.NEQ;
                    // (-inf, num), num, [num + 1, inf)
                    intset.add(num);
                    intset.add(num + 1);
                    break;
                case StateExprParser.GT:
                    op = State.CompareOperator.GT;
                    // (-inf, num + 1), [num + 1, inf)
                    intset.add(num + 1);
                    break;
                case StateExprParser.LT:
                    op = State.CompareOperator.LT;
                    // (-inf, num), [num, inf)
                    intset.add(num);
                    break;
                case StateExprParser.LTEQ:
                    op = State.CompareOperator.LTEQ;
                    // (-inf, num + 1), [num + 1, inf)
                    intset.add(num + 1);
                    break;
                case StateExprParser.GTEQ:
                    op = State.CompareOperator.GTEQ;
                    // (-inf, num), [num, inf)
                    intset.add(num);
                    break;
                default:
                    throw new NoSuchMethodError(ctx.compOp.getText());
            }

            return new State.CompareExpr(
                    name,
                    num,
                    op);
        }

        @Override
        public State.Expr visitLogic2Expr(StateExprParser.Logic2ExprContext ctx) {
            State.LogicOperator op;
            switch (ctx.boolOp.getType()) {
                case StateExprParser.AND:
                    op = State.LogicOperator.AND;
                    break;
                case StateExprParser.OR:
                    op = State.LogicOperator.OR;
                    break;
                default:
                    throw new NoSuchMethodError(ctx.boolOp.getText());
            }
            return new State.Logic2Expr(
                    visit(ctx.expr(0)),
                    visit(ctx.expr(1)),
                    op);
        }

        @Override
        public State.Expr visitParenExpr(StateExprParser.ParenExprContext ctx) {
            return visit(ctx.expr());
        }
    }

    public static State parse(String input) {
        input = input.trim();

        ANTLRInputStream is = new ANTLRInputStream(input);
        ca.uqac.phoenixxie.ltl.antlr4.StateExprLexer lexer = new ca.uqac.phoenixxie.ltl.antlr4.StateExprLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        StateExprParser parser = new StateExprParser(tokens);

        parser.removeErrorListeners();
        final StringBuilder sbErr = new StringBuilder();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                sbErr.append(msg).append("\n");
            }
        });

        StateExprParser.ProgContext tree = parser.prog();

        Vistor vistor = new Vistor();
        State.Expr expr = vistor.visit(tree);

        State state = new State();
        state.expr = input;
        state.success = parser.getNumberOfSyntaxErrors() == 0;
        state.errorMsg = sbErr.toString();
        state.stateExpr = expr;
        state.variables = vistor.getVars();

        return state;
    }


}
