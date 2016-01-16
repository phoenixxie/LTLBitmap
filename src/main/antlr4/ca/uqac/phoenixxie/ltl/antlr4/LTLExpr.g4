grammar LTLExpr;

prog
    :   expr EOF
    ;

expr
    :   '(' expr ')'                                           #parenExpr
    |   op=(NOT|NEXT|FUTURE|GLOBAL) expr                       #op1Expr
    |   left=expr op=(UNTIL|RELEASE|WEAKLYUNTIL) right=expr    #op2Expr
    |   left=expr op=(AND|OR) right=expr                       #andOrExpr
    |   left=expr op=THEN right=expr                           #thenExpr
    |   state=STATE                                            #state
    ;

// propositional logic
NOT: '!';
AND: '&&';
OR: '||';
THEN: '->';

// temporal logic
NEXT: 'X';
FUTURE: 'F';
GLOBAL: 'G';
UNTIL: 'U';
RELEASE: 'R';
WEAKLYUNTIL: 'W';

STATE: 's'[0-9]+;
WS  :   [ \t\r\n] -> channel(HIDDEN);