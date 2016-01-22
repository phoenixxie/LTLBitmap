package ca.uqac.phoenixxie.ltl.analyze;

import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;

public class Formula {
    boolean success;
    String expr;
    String errorMsg;
    Expr ltlExpr;
    int maxStateID;

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

    public int getMaxStateID() {
        return maxStateID;
    }

    enum Op1Operator {
        NOT,
        NEXT,
        FUTURE,
        GLOBAL
    }

    enum Op2Operator {
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

    static class StateExpr implements Expr {
        private int stateIndex;

        public StateExpr(int stateIndex) {
            this.stateIndex = stateIndex;
        }

        @Override
        public LTLBitmap getResult(LTLBitmap[] states) {
            return states[stateIndex];
        }
    }

    static class Op1Expr implements Expr {
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

    static class Op2Expr implements Expr {
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

}
