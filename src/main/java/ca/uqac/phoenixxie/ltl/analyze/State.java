package ca.uqac.phoenixxie.ltl.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class State {
    enum ExprType {
        COMPARE,
        LOGIC
    }

    enum CompareOperator {
        NEQ, EQ, GT, LT, GTEQ, LTEQ
    }

    enum LogicOperator {
        AND, OR
    }

    boolean success;
    String expr;
    String errorMsg;
    Expr stateExpr;
    HashMap<String, Integer[]> variables;

    public HashMap<String, Integer[]> getVariables() {
        return variables;
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

    public Expr getStateExpr() {
        return stateExpr;
    }

    public interface Expr {
        boolean getResult(HashMap<String, Integer> values);
    }

    static class CompareExpr implements Expr {
        private String name;
        private int rval;
        private CompareOperator operator;

        public CompareExpr(String name, int value, CompareOperator operator) {
            this.name = name;
            this.rval = value;
            this.operator = operator;
        }

        @Override
        public boolean getResult(HashMap<String, Integer> values) {
            if (!values.containsKey(name)) {
                throw new NoSuchElementException(name);
            }

            int lval = values.get(name);
            switch (operator) {
                case NEQ:
                    return lval != rval;
                case EQ:
                    return lval == rval;
                case GT:
                    return lval > rval;
                case LT:
                    return lval < rval;
                case GTEQ:
                    return lval >= rval;
                case LTEQ:
                    return lval <= rval;
            }
            throw new NoSuchMethodError(operator.toString());
        }
    }

    static class Logic2Expr implements Expr {
        private Expr lexpr;
        private Expr rexpr;
        private LogicOperator operator;

        public Logic2Expr(Expr lexpr, Expr rexpr, LogicOperator operator) {
            this.lexpr = lexpr;
            this.rexpr = rexpr;
            this.operator = operator;
        }

        @Override
        public boolean getResult(HashMap<String, Integer> values) {
            switch (operator) {
                case AND:
                    return (lexpr.getResult(values) && rexpr.getResult(values));
                case OR:
                    return (lexpr.getResult(values) || rexpr.getResult(values));
            }
            throw new NoSuchMethodError(operator.toString());
        }
    }

    static class LogicNoExpr implements Expr {
        private Expr expr;

        public LogicNoExpr(Expr expr) {
            this.expr = expr;
        }

        @Override
        public boolean getResult(HashMap<String, Integer> values) {
            return !expr.getResult(values);
        }
    }

}
