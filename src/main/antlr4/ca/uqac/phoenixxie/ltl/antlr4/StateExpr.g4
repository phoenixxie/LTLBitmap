grammar StateExpr;

prog
    :   expr EOF
    ;

expr
    :   '(' expr ')'
    |   (TRUE|FALSE)
    |   (VAR|NUMBER) compOp (VAR|NUMBER)
    |   NOT expr
    |   expr boolOp expr
    ;

compOp: NEQ|EQ|GT|LT|GTEQ|LTEQ;
boolOp: AND|OR;

TRUE: 'TRUE';
FALSE: 'FALSE';

NEQ: '!=';
EQ: '=';
GT: '>';
LT: '<';
GTEQ: '>=';
LTEQ: '<=';
AND: '&&';
OR: '||';
NOT: '!';

VAR: [a-zA-Z]+[a-zA-Z0-9]*;
NUMBER: MINUS? [0-9]+;
MINUS: '-';

WS  :   [ \t\r\n] -> channel(HIDDEN);