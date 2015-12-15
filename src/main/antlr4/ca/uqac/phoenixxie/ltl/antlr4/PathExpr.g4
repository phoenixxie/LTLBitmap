grammar PathExpr;

prog
    :   expr EOF
    ;

expr
    :   '(' expr ')'
    |   op=(NOT|NEXT|FUTURE|GLOBAL) expr
    |   left=expr op=(UNTIL|RELEASE|WEAKLYUNTIL) right=expr
    |   left=expr op=(AND|OR) right=expr
    |   left=expr op=THEN right=expr
    |   state=(TRUE|FALSE|STATE)
    ;

TRUE : 'TRUE';
FALSE  : 'FALSE';

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

STATE: 's'[A-Z0-9]+;
WS  :   [ \t\r\n] -> channel(HIDDEN);