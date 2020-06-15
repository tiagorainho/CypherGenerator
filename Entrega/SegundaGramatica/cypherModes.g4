grammar cypherModes;

program: fileName use (line)* EOF;

fileName: 'CypherMode' ID '->' NUM ',' NUM ';';

interval
	returns[int[] size = null]: '[' NUM ',' NUM ']';

line: action;

use: 'use' 'cypher' ID ';';

action:
	cypherFunction ';'	# ActionCypherFunction
	| declare ';'		# ActionDeclare
	| ifCond			# ActionIfCond
	| forLoop			# ActionForLoop
	| assignment ';'	# ActionAssign;

ifCond:
	'ifNotEquals' '(' arg ',' arg ')' (line)* '$ifNotEquals';

arg: (variable | NUM);

forLoop: 'increment' ID 'forEach' ID (line)* '$forEach';

declare: types variableNames (interval)? '<-' value;

variableNames: ID (',' ID)*;

types: 'num' | 'byte';

assignment: variable (',' variable)* '<-' value;

variable
	locals[String type = ""]: ID coords?;

value
	returns[int[] size = null,String type = ""]:
	variable			# ValueID
	| BYTE				# ValueBYTE
	| matrix			# ValueMatrix
	| cypherFunction	# ValueCypherFunction
	| colSize			# ValueColSize
	| NUM				# ValueNUM
	| operation			# ValueOperation;

colSize
	locals[String type = ""]: 'colMaxIndex' '(' ID ')';

cypherFunction
	returns[String type = ""]: ID '(' functionArgs ')';

functionArgs
	returns[String type = ""]: ID coords?;

operation
	locals[String type = ""]:
	'(' operation ')'			# OperParentheses
	| operation 'xor' operation	# OperXor
	| operation '+' operation	# OperPlus
	| NUM						# OperNum
	| BYTE						# OperByte
	| variable					# OperID;

matrix
	returns[int[] size = null, String type = null]:
	lineMatrix ('|' lineMatrix)*;

lineMatrix
	returns[int c = 0,String type = null]: (NUM (',' NUM)+)
	| (BYTE (',' BYTE)+);

coords: '[' coord ',' coord ']';

coord: (NUM | operation | ID);

NUM: [0-9]+ ('.' [0-9]+)?;
BYTE: '0x' ([0-9] | [a-fA-F])? ([0-9] | [a-fA-F]);
ID: 'data' | [a-zA-Z0-9_]+;
WS: [ \t\r\n] -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
COMMENT: '/*' .*? '*/' -> skip;
NEWLINE: '\r'? '\n';
ERROR: .;