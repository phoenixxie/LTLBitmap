package ca.uqac.phoenixxie.ltl.analyze;

import ca.uqac.phoenixxie.ltl.antlr4.LTLExprBaseVisitor;
import ca.uqac.phoenixxie.ltl.antlr4.LTLExprParser;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;
import org.antlr.v4.runtime.*;

public class LTLParser {

    public static class Result {
        boolean success;
        String expr;
        String errorMsg;
        Expr ltlExpr;

        public Expr getLtlExpr() {
            return ltlExpr;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getExpr() {
            return expr;
        }
    }

    private enum Op1Operator {
        NOT,
        NEXT,
        FUTURE,
        GLOBAL
    }

    private enum Op2Operator {
        UNTIL,
        RELEASE,
        WEAKLYUNTIL,
        AND,
        OR,
        THEN
    }

    public interface Expr {
        LTLBitmap getResult(LTLBitmap[] states);
    }

    private static class StateExpr implements Expr {
        private int stateIndex;

        public StateExpr(int stateIndex) {
            this.stateIndex = stateIndex;
        }

        @Override
        public LTLBitmap getResult(LTLBitmap[] states) {
            return states[stateIndex];
        }
    }

    private static class Op1Expr implements Expr {
        private Op1Operator operator;
        private Expr expr;

        public Op1Expr(Expr expr, Op1Operator operator) {
            this.operator = operator;
            this.expr = expr;
        }

        @Override
        public LTLBitmap getResult(LTLBitmap[] states) {
            switch (operator) {
                case NOT:
                    return expr.getResult(states).opNot();
                case NEXT:
                    return expr.getResult(states).opNext();
                case FUTURE:
                    return expr.getResult(states).opFuture();
                case GLOBAL:
                    return expr.getResult(states).opGlobal();
            }
            throw new NoSuchMethodError(operator.toString());
        }
    }

    private static class Op2Expr implements Expr {
        private Expr lexpr;
        private Expr rexpr;
        private Op2Operator operator;

        public Op2Expr(Expr lexpr, Expr rexpr, Op2Operator operator) {
            this.lexpr = lexpr;
            this.rexpr = rexpr;
            this.operator = operator;
        }

        @Override
        public LTLBitmap getResult(LTLBitmap[] states) {
            LTLBitmap l = lexpr.getResult(states);
            LTLBitmap r = rexpr.getResult(states);
            switch (operator) {
                case UNTIL:
                    return l.opUntil(r);
                case RELEASE:
                    return l.opRelease(r);
                case WEAKLYUNTIL:
                    return l.opWeakUntil(r);
                case AND:
                    return l.opAnd(r);
                case OR:
                    return l.opOr(r);
                case THEN:
                    return l.opThen(r);
            }
            throw new NoSuchMethodError(operator.toString());
        }
    }

    private static class Vistor extends LTLExprBaseVisitor<Expr> {
        @Override
        public Expr visitProg(LTLExprParser.ProgContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public Expr visitOp2Expr(LTLExprParser.Op2ExprContext ctx) {
            Op2Operator operator;
            switch (ctx.op.getType()) {
                case LTLExprParser.RELEASE: operator = Op2Operator.RELEASE; break;
                case LTLExprParser.UNTIL: operator = Op2Operator.UNTIL; break;
                case LTLExprParser.WEAKLYUNTIL: operator = Op2Operator.WEAKLYUNTIL; break;
                default: throw new NoSuchMethodError(ctx.op.getText());
            }
            return new Op2Expr(visit(ctx.left), visit(ctx.right), operator);
        }

        @Override
        public Expr visitOp1Expr(LTLExprParser.Op1ExprContext ctx) {
            Op1Operator operator;
            switch (ctx.op.getType()) {
                case LTLExprParser.FUTURE: operator = Op1Operator.FUTURE; break;
                case LTLExprParser.GLOBAL: operator = Op1Operator.GLOBAL; break;
                case LTLExprParser.NEXT: operator = Op1Operator.NEXT; break;
                case LTLExprParser.NOT: operator = Op1Operator.NOT; break;
                default: throw new NoSuchMethodError(ctx.op.getText());
            }
            return new Op1Expr(visit(ctx.expr()), operator);
        }

        @Override
        public Expr visitState(LTLExprParser.StateContext ctx) {
            String name = ctx.state.getText().substring(1);
            int index = Integer.parseInt(name);
            return new StateExpr(index);
        }

        @Override
        public Expr visitAndOrExpr(LTLExprParser.AndOrExprContext ctx) {
            Op2Operator operator;
            switch (ctx.op.getType()) {
                case LTLExprParser.AND: operator = Op2Operator.AND; break;
                case LTLExprParser.OR: operator = Op2Operator.OR; break;
                default: throw new NoSuchMethodError(ctx.op.getText());
            }
            return new Op2Expr(visit(ctx.left), visit(ctx.right), operator);
        }

        @Override
        public Expr visitThenExpr(LTLExprParser.ThenExprContext ctx) {
            return new Op2Expr(visit(ctx.left), visit(ctx.right), Op2Operator.THEN);
        }

        @Override
        public Expr visitParenExpr(LTLExprParser.ParenExprContext ctx) {
            return visit(ctx.expr());
        }
    }


    public static Result parse(String input) {
        input = input.trim();

        ANTLRInputStream is = new ANTLRInputStream(input);
        ca.uqac.phoenixxie.ltl.antlr4.LTLExprLexer lexer = new ca.uqac.phoenixxie.ltl.antlr4.LTLExprLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LTLExprParser parser = new LTLExprParser(tokens);

        parser.removeErrorListeners();

        final StringBuilder sbErr = new StringBuilder();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                sbErr.append(msg).append("\n");
            }
        });

        LTLExprParser.ProgContext tree = parser.prog();
        Vistor visitor = new Vistor();
        Expr expr = visitor.visit(tree);

        Result ret = new Result();
        ret.expr = input;
        ret.ltlExpr = expr;
        ret.success = parser.getNumberOfSyntaxErrors() == 0;
        ret.errorMsg = sbErr.toString();

        return ret;
    }

}
