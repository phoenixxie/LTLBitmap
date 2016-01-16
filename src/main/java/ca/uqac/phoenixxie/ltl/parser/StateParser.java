package ca.uqac.phoenixxie.ltl.parser;

import ca.uqac.phoenixxie.ltl.antlr4.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class StateParser {
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

    public interface Expr {
        boolean getResult(HashMap<String, Integer> values);
    }

    private static class CompareExpr implements Expr {
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

    private static class Logic2Expr implements Expr {
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
                    return (lexpr.getResult(values) && rexpr.getResult(values));
            }
            throw new NoSuchMethodError(operator.toString());
        }
    }

    private static class LogicNoExpr implements Expr {
        private Expr expr;

        public LogicNoExpr(Expr expr) {
            this.expr = expr;
        }

        @Override
        public boolean getResult(HashMap<String, Integer> values) {
            return !expr.getResult(values);
        }
    }

    private static class Vistor extends StateExprBaseVisitor<Expr> {
        @Override
        public Expr visitProg(StateExprParser.ProgContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public Expr visitNoExpr(StateExprParser.NoExprContext ctx) {
            return new LogicNoExpr(visit(ctx.expr()));
        }

        @Override
        public Expr visitCompExpr(StateExprParser.CompExprContext ctx) {
            CompareOperator op;
            switch (ctx.compOp.getType()) {
                case StateExprParser.EQ:   op = CompareOperator.EQ; break;
                case StateExprParser.GT:   op = CompareOperator.GT; break;
                case StateExprParser.LT:   op = CompareOperator.LT; break;
                case StateExprParser.LTEQ: op = CompareOperator.LTEQ; break;
                case StateExprParser.GTEQ: op = CompareOperator.GTEQ; break;
                case StateExprParser.NEQ:  op = CompareOperator.NEQ; break;
                default:
                    throw new NoSuchMethodError(ctx.compOp.getText());
            }
            return new CompareExpr(
                    ctx.VAR().getText(),
                    Integer.parseInt(ctx.NUMBER().getText()),
                    op);
        }

        @Override
        public Expr visitLogic2Expr(StateExprParser.Logic2ExprContext ctx) {
            LogicOperator op;
            switch (ctx.boolOp.getType()) {
                case StateExprParser.AND: op = LogicOperator.AND; break;
                case StateExprParser.OR:  op = LogicOperator.OR; break;
                default:
                    throw new NoSuchMethodError(ctx.boolOp.getText());
            }
            return new Logic2Expr(
                    visit(ctx.expr(0)),
                    visit(ctx.expr(1)),
                    op);
        }

        @Override
        public Expr visitParenExpr(StateExprParser.ParenExprContext ctx) {
            return visit(ctx.expr());
        }
    }

    public static class Result {
        boolean success;
        String expr;
        StateExprParser.ProgContext tree;
        String errorMsg;
        Expr stateExpr;

        public ParseTree getTree() {
            return tree;
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
    }

    public static Result parseState(String input) {
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
        Expr expr = vistor.visit(tree);

        Result ret = new Result();
        ret.expr = input;
        ret.stateExpr = expr;
        ret.success = parser.getNumberOfSyntaxErrors() == 0;
        ret.tree = tree;
        ret.errorMsg = sbErr.toString();

        return ret;
    }


}
