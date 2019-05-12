#!/bin/bash
clear
PROGRAM=Sudoku
# ------------------------------------------------------------------------------
function execute() {
	echo "$@"
	$@
	pause
} # function pause()
# ------------------------------------------------------------------------------
function pause() {
	echo
	read -p "Press Enter to continue" X
	echo
} # function pause()
# ------------------------------------------------------------------------------
ONCE=1
ERRNO=0
COMPILER_OPTIONS="-Xmaxerrs 10 -Xmaxwarns 10"
for SOURCE in Area Coordinate Print Position Cell Matrix Sudoku
do
	JAVA=$SOURCE.java
	CLASS=$SOURCE.class
	if [ $JAVA -nt $CLASS ]
	then
		[ $ONCE = 1 ] && echo ===== COMPILING ===== && ONCE=0
		echo javac $COMPILER_OPTIONS $JAVA
		javac $COMPILER_OPTIONS $JAVA
		ERRNO=$?
		[ $ERRNO != 0 ] && break
	fi
done
if [ $ERRNO == 0 ]
then
	if [ $# == 0 ]
	then
		echo ===== TEST =====
		# --coordinate
		[ $? = 0 ] && execute java $PROGRAM -c CHESS  < $PROGRAM.0002
		[ $? = 0 ] && execute java $PROGRAM -c JAVA   < $PROGRAM.0002
		[ $? = 0 ] && execute java $PROGRAM -c ROWCOL < $PROGRAM.0002
		[ $? = 0 ] && execute java $PROGRAM -c SUDOKU < $PROGRAM.0002
		# --go
		[ $? = 0 ] && execute java $PROGRAM -c CHESS  -g 9a   < $PROGRAM.0000
		[ $? = 0 ] && execute java $PROGRAM -c JAVA   -g 00   < $PROGRAM.0000
		[ $? = 0 ] && execute java $PROGRAM -c ROWCOL -g r1c1 < $PROGRAM.0000
		[ $? = 0 ] && execute java $PROGRAM -c SUDOKU -g a1   < $PROGRAM.0000
		# --method FULL_HOUSE
		[ $? = 0 ] && execute java $PROGRAM -g 04 -s -m FULL_HOUSE -v < $PROGRAM.0008
		[ $? = 0 ] && execute java $PROGRAM -g 53 -s -m FULL_HOUSE -v < $PROGRAM.0008
		[ $? = 0 ] && execute java $PROGRAM -g 38 -s -m FULL_HOUSE -v < $PROGRAM.0008
		# --method NAKED_SINGLE
		[ $? = 0 ] && execute java $PROGRAM -g 00 -s -m NAKED_SINGLE -v < $PROGRAM.0000
		[ $? = 0 ] && execute java $PROGRAM -g 14 -s -m NAKED_SINGLE -v < $PROGRAM.0009
		# --method HIDDEN_SINGLE
		[ $? = 0 ] && execute java $PROGRAM -g 00 -s -m HIDDEN_SINGLE -v < $PROGRAM.0001
		[ $? = 0 ] && execute java $PROGRAM -g 00 -s -m HIDDEN_SINGLE -v < $PROGRAM.0004
		[ $? = 0 ] && execute java $PROGRAM -g 00 -s -m HIDDEN_SINGLE -v < $PROGRAM.0005
		# --solve --verbose
		[ $? = 0 ] && execute java $PROGRAM -s -v < $PROGRAM.0002 # 30 - solved
		[ $? = 0 ] && execute java $PROGRAM -s -v < $PROGRAM.0003 # 27
		[ $? = 0 ] && execute java $PROGRAM -s -v < $PROGRAM.0006 # 24
		[ $? = 0 ] && execute java $PROGRAM -s -v < $PROGRAM.0007 # 23
	else
		echo ===== DEMO =====
		execute java $PROGRAM --help
		execute java $PROGRAM --interactive
		execute java $PROGRAM --coordinate SUDOKU < $PROGRAM.0002
		execute java $PROGRAM --analyze < $PROGRAM.0002
		execute java $PROGRAM --solve < $PROGRAM.0002
		execute java $PROGRAM -s --verbose < $PROGRAM.0002
		execute java $PROGRAM -s --go 41 --debug < $PROGRAM.0006
		execute java $PROGRAM -s --method NAKED_SINGLE < $PROGRAM.0000
		execute java $PROGRAM -s -m NAKED_SINGLE -m FULL_HOUSE < $PROGRAM.0001
	fi
fi
