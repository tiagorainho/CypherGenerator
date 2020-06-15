grammar cripto;


program: init EOF;

init: fileName (import1)* (line | function)* ;

fileName: 'Cypher' ID '->' NUM ',' NUM ';';
import1
	returns[String strvalue]:
	'import' ID ';'; 

line: action;
action:
	declare ';'			# ActionDeclare
	| forLoop			# ActionForLoop
	| ifCond			# ActionIfCond
	| whileLoop			# ActionWhileCond
	| commands ';'		# ActionCommands
	| assignment ';'	# ActionAssign;

interval
	returns[int[] size = null]: '[' NUM ',' NUM ']';

blockCode: (line)* (
		'out' '<-' (getCommands | ID | BYTE | BIT | NUM) ';'
	)?;

declare: types variableNames (interval)? '<-' value;

assignment: (variableNames) '<-' value;

value
	returns[int[] size = null,String type = ""]:
	getCommands			# ValueGetCommands
	| predefined_Matrix	# ValuePredefined_Matrix
	| NUM				# ValueNUM
	| BIT				# ValueBIT
	| ID				# ValueID
	| BYTE				# ValueBYTE
	| matrix			# ValueMatrix
	| operators			# ValueOperators;

variableNames: ID (',' ID)*;

matrix
	returns[int[] size = null, String type = null]:
	lineMatrix ('|' lineMatrix)*;

lineMatrix
	returns[int c = 0,String type = null]: (NUM (',' NUM)+)
	| (BIT (',' BIT)+)
	| (BYTE (',' BYTE)+);

function:
	'func' 'out' '<-' (functionTypes | 'nothing') ID '(' (
		(functionTypes ID) (',' functionTypes ID)*
	)? ')' blockCode '$func';

functionTypes:
	'bit-M'
	| 'num-M'
	| 'byte-M'
	| 'bit'
	| 'num'
	| 'byte';

types: 'bit' | 'num' | 'byte';

getCommands
	returns[String type = null]:
	'Get' 'Value' 'from' '[' (
		ID
		| predefined_Matrix
		| getCommands
	) ',' coords ']' # GetValueCommands
	| 'Get' 'Matrix' 'from' '[' (
		ID
		| predefined_Matrix
		| getCommands
	) ',' coords ',' coords ']' # GetMatrixCommands
	| 'Get' 'Row' 'from' '[' (
		ID
		| predefined_Matrix
		| getCommands
	) ',' (NUM | ID) ']' # GetRowCommands
	| 'Get' 'Col' 'from' '[' (
		ID
		| predefined_Matrix
		| getCommands
	) ',' (NUM | ID) ']'														# GetColCommands
	| 'Zeros' '[' types ',' coords ']'											# ZerosCommands
	| 'To' 'Line' '[' (ID | predefined_Matrix) ']'								# ToLineCommands
	| 'To' 'Matrix' '[' (ID | predefined_Matrix) ',' coords ']'					# ToMatrixCommands
	| 'To' 'Num' '[' (ID | BYTE | BIT | operators | getCommands) ']'			# ToNumCommands
	| 'To' 'Byte' '[' (ID | NUM | BIT | operators | getCommands) ']'			# ToByteCommands
	| 'To' 'Bit' '[' (ID | NUM | BYTE | operators | getCommands) ']'			# ToBitCommands
	| userFunc																	# GetUserFuncCommands;

userFunc: ID '(' (userArg (',' userArg)*)? ')';

userArg: NUM | BYTE | BIT | getCommands | ID;

commands:'Sub' 'Bytes' '[' ID ',' (
		ID
		| predefined_Matrix
	) ']'									# SubCommands
	| 'Shift' 'Row' '[' ID ',' coords ']'	# ShiftRowsCommands
	| 'Shift' 'Col' '[' ID ',' coords ']'	# ShiftColsCommands
	| 'Set' 'Value' '[' ID ',' (
		BIT
		| BYTE
		| NUM
		| ID
		| getCommands
	) ',' coords ']'												# SetValueCommands
	| 'Set' 'Matrix' '[' (ID) ',' ID ',' coords ']'					# SetMatrixCommands
	| 'Set' 'Row' '[' ID ',' (ID | getCommands) ',' (NUM | ID) ']'	# SetRowCommands
	| 'Set' 'Col' '[' ID ',' (ID | getCommands) ',' (NUM | ID) ']'	# SetColCommands
	| 'Rotate' 'Col' '[' ID ',' NUM ',' NUM ']'						# RotateColCommands
	| 'Rotate' 'Line' '[' ID ',' NUM ',' NUM ']'					# RotateLineCommands
	| 'Switch' 'Axis' '[' ID ']'									# SwitchAxisCommands
	| userFunc														# UserFuncCommands;

predefined_Matrix
	returns[String type = null]:
	'Get_rijndael_s_box' '(' ')'			# predefined_MatrixS_Box
	| 'Get_rijndael_s_box_inverted' '(' ')'	# predefined_MatrixS_BoxInverted
	| 'Get_rcon' '(' ')'					# predefined_MatrixS_rcon
	| 'Get_L' '(' ')'						# predefined_MatrixL
	| 'Get_E' '(' ')'						# predefined_MatrixE;

coords: coord ',' coord;

coord: (NUM | ID | getCommands);

cond locals[String type = ""]:
	cond OPERATOR cond	# CondOperation
	| '(' cond ')'		# CondParent
	| ID				# CondVarExpr
	| NUM				# CondNUM
	| BIT				# CondBIT
	| BYTE				# CondBYTE
	| '(' operators ')'	# CondOperators;

operators locals[String type = ""]:
	'(' operators ')'												# OperParentheses
	| operators '^' operators										# OperPow
	| operators op = ('*' | '\\' | '%') operators					# OperDivMultRest
	| operators op = ('+' | '-' | 'xor' | 'and' | 'or') operators	# OperAddSub
	| getCommands													# OperGetCommands
	| NUM															# OperNum
	| BYTE															# OperByte
	| BIT															# OperBit
	| ID															# OperID;

whileLoop: 'while' '(' cond ')' blockCode '$while';

ifCond: 'if' '(' cond ')' blockCode '$if';

forLoop: 'for' '(' forLeft ':' forRight ')' blockCode '$for';

forLeft
	returns[String var = null, String min = null]:
	declareFor		# ForLDeclareOption
	| assignmentFor	# ForAssign
	| ID			# ForLVariable
	| NUM			# ForLNumber;

forRight
	returns[String n = null]:
	ID		# ForRVariable
	| NUM	# ForRNumber;

assignmentFor
	returns[String valAssignF = null]: ID '<-' (NUM | ID);

declareFor
	returns[String valDeclareF = null]: 'num' ID '<-' (NUM | ID);

OPERATOR: (
		'='
		| '>='
		| '>'
		| '<'
		| '<='
		| '!='
		| '&&'
		| '||'
	);

NUM: [0-9]+ ('.' [0-9]+)?;
BYTE: '0x' ([0-9] | [a-fA-F])? ([0-9] | [a-fA-F]);
BIT: '0b' [0-1];
ID: 'data' | 'key' | [a-zA-Z0-9_]+;
WS: [ \t\r\n] -> skip;
fragment ESC: '\\"' | '\\\\';
LINE_COMMENT: '//' ~[\r\n]* -> skip;
COMMENT: '/*' .*? '*/' -> skip;
NEWLINE: '\r'? '\n';
ERROR: .;