grammar StateExpr;

prog
    :   expr EOF
    ;

expr
    :   '(' expr ')'                               # parenExpr
    |   VAR compOp=(NEQ|EQ|GT|LT|GTEQ|LTEQ) NUMBER # compExpr
    |   NOT expr                                   # noExpr
    |   expr boolOp=(AND|OR) expr                  # logic2Expr
    ;

NEQ: '!=';
EQ: '=';
GT: '>';
LT: '<';
GTEQ: '>=';
LTEQ: '<=';
AND: '&&';
OR: '||';
NOT: '!';

VAR: [a-z][a-z0-9]*;
NUMBER: MINUS? [0-9]+;
MINUS: '-';

WS  :   [ \t\r\n] -> channel(HIDDEN);