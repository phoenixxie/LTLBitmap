package ca.uqac.phoenixxie.ltl.parser;

import ca.uqac.phoenixxie.ltl.antlr4.PathExprParser;
import ca.uqac.phoenixxie.ltl.antlr4.StateExprParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class LTLParser {
    public static class StateResult {
        boolean success;
        String expr;
        StateExprParser.ProgContext tree;
        String errorMsg;

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
    }

    public static class PathResult {
        boolean success;
        String expr;
        PathExprParser.ProgContext tree;
        String errorMsg;

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
    }

    public static StateResult parseState(String input) {
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

        StateResult ret = new StateResult();
        ret.expr = input;
        ret.success = parser.getNumberOfSyntaxErrors() == 0;
        ret.tree = tree;
        ret.errorMsg = sbErr.toString();

        return ret;
    }

    public static PathResult parsePath(String input) {
        ANTLRInputStream is = new ANTLRInputStream(input);
        ca.uqac.phoenixxie.ltl.antlr4.PathExprLexer lexer = new ca.uqac.phoenixxie.ltl.antlr4.PathExprLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PathExprParser parser = new PathExprParser(tokens);

        parser.removeErrorListeners();

        final StringBuilder sbErr = new StringBuilder();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                sbErr.append(msg).append("\n");
            }
        });

        PathExprParser.ProgContext tree = parser.prog();

        PathResult ret = new PathResult();
        ret.expr = input;
        ret.success = parser.getNumberOfSyntaxErrors() == 0;
        ret.tree = tree;
        ret.errorMsg = sbErr.toString();

        return ret;
    }

}
