package ca.uqac.phoenixxie.ltl.parser;

import ca.uqac.phoenixxie.ltl.antlr4.StateExprParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class LTLParser {
    public static boolean checkState(String input) {
        ANTLRInputStream is = new ANTLRInputStream(input);
        ca.uqac.phoenixxie.ltl.antlr4.StateExprLexer lexer = new ca.uqac.phoenixxie.ltl.antlr4.StateExprLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        StateExprParser parser = new StateExprParser(tokens);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
//                super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
                System.err.println(msg);
            }
        });

        ParseTree tree = parser.prog();

        System.out.println(tree.toStringTree(parser));
        System.out.println(parser.getNumberOfSyntaxErrors());

        return true;
    }
}
