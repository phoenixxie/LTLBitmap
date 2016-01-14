grammar StateExpr;

prog
    :   expr EOF
    ;

expr
    :   '(' expr ')'
    |   VAR compOp NUMBER
    |   NOT expr
    |   expr boolOp expr
    ;

compOp: NEQ|EQ|GT|LT|GTEQ|LTEQ;
boolOp: AND|OR;

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