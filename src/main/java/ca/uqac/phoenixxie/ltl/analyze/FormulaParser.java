package ca.uqac.phoenixxie.ltl.analyze;

import ca.uqac.phoenixxie.ltl.analyze.Formula.Expr;
import ca.uqac.phoenixxie.ltl.antlr4.LTLExprBaseVisitor;
import ca.uqac.phoenixxie.ltl.antlr4.LTLExprParser;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;
import org.antlr.v4.runtime.*;

public class FormulaParser {

    private static class Vistor extends LTLExprBaseVisitor<Expr> {
        private int maxStateID = -1;

        public int getMaxStateID() {
            return maxStateID;
        }

        @Override
        public Expr visitProg(LTLExprParser.ProgContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public Expr visitOp2Expr(LTLExprParser.Op2ExprContext ctx) {
            Formula.Op2Operator operator;
            switch (ctx.op.getType()) {
                case LTLExprParser.RELEASE: operator = Formula.Op2Operator.RELEASE; break;
                case LTLExprParser.UNTIL: operator = Formula.Op2Operator.UNTIL; break;
                case LTLExprParser.WEAKLYUNTIL: operator = Formula.Op2Operator.WEAKLYUNTIL; break;
                default: throw new NoSuchMethodError(ctx.op.getText());
            }
            return new Formula.Op2Expr(visit(ctx.left), visit(ctx.right), operator);
        }

        @Override
        public Expr visitOp1Expr(LTLExprParser.Op1ExprContext ctx) {
            Formula.Op1Operator operator;
            switch (ctx.op.getType()) {
                case LTLExprParser.FUTURE: operator = Formula.Op1Operator.FUTURE; break;
                case LTLExprParser.GLOBAL: operator = Formula.Op1Operator.GLOBAL; break;
                case LTLExprParser.NEXT: operator = Formula.Op1Operator.NEXT; break;
                case LTLExprParser.NOT: operator = Formula.Op1Operator.NOT; break;
                default: throw new NoSuchMethodError(ctx.op.getText());
            }
            return new Formula.Op1Expr(visit(ctx.expr()), operator);
        }

        @Override
        public Expr visitState(LTLExprParser.StateContext ctx) {
            String name = ctx.state.getText().substring(1);
            int index = Integer.parseInt(name);
            if (index > maxStateID) {
                maxStateID = index;
            }
            return new Formula.StateExpr(index);
        }

        @Override
        public Expr visitAndOrExpr(LTLExprParser.AndOrExprContext ctx) {
            Formula.Op2Operator operator;
            switch (ctx.op.getType()) {
                case LTLExprParser.AND: operator = Formula.Op2Operator.AND; break;
                case LTLExprParser.OR: operator = Formula.Op2Operator.OR; break;
                default: throw new NoSuchMethodError(ctx.op.getText());
            }
            return new Formula.Op2Expr(visit(ctx.left), visit(ctx.right), operator);
        }

        @Override
        public Expr visitThenExpr(LTLExprParser.ThenExprContext ctx) {
            return new Formula.Op2Expr(visit(ctx.left), visit(ctx.right), Formula.Op2Operator.THEN);
        }

        @Override
        public Expr visitParenExpr(LTLExprParser.ParenExprContext ctx) {
            return visit(ctx.expr());
        }
    }


    public static Formula parse(String input) {
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

        Formula ret = new Formula();
        ret.expr = input;
        ret.ltlExpr = expr;
        ret.maxStateID = visitor.maxStateID;
        ret.success = parser.getNumberOfSyntaxErrors() == 0;
        ret.errorMsg = sbErr.toString();

        return ret;
    }

}
