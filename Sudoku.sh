#!/bin/bash
clear
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
		# [ $? = 0 ] && execute java Sudoku -c CHESS  < Sudoku.0002
		# [ $? = 0 ] && execute java Sudoku -c JAVA   < Sudoku.0002
		# [ $? = 0 ] && execute java Sudoku -c ROWCOL < Sudoku.0002
		# [ $? = 0 ] && execute java Sudoku -c SUDOKU < Sudoku.0002
		# --go
		# [ $? = 0 ] && execute java Sudoku -c CHESS  -g 9a   < Sudoku.0000
		# [ $? = 0 ] && execute java Sudoku -c JAVA   -g 00   < Sudoku.0000
		# [ $? = 0 ] && execute java Sudoku -c ROWCOL -g r1c1 < Sudoku.0000
		# [ $? = 0 ] && execute java Sudoku -c SUDOKU -g a1   < Sudoku.0000
		# --method FULL_HOUSE
		# [ $? = 0 ] && execute java Sudoku -g 04 -s -m FULL_HOUSE -v < Sudoku.0008
		# [ $? = 0 ] && execute java Sudoku -g 53 -s -m FULL_HOUSE -v < Sudoku.0008
		# [ $? = 0 ] && execute java Sudoku -g 38 -s -m FULL_HOUSE -v < Sudoku.0008
		# --method NAKED_SINGLE
		# [ $? = 0 ] && execute java Sudoku -g 00 -s -m NAKED_SINGLE -v < Sudoku.0000
		# [ $? = 0 ] && execute java Sudoku -g 14 -s -m NAKED_SINGLE -v < Sudoku.0009
		# --method NAKED_PAIR
		# [ $? = 0 ] && execute java Sudoku -g 50 -s -m NAKED_PAIR -v -d < Sudoku.0010
		# [ $? = 0 ] && execute java Sudoku -g 26 -s -m NAKED_PAIR -v -d < Sudoku.0011
		# --method NAKED_TRIPLE
		# [ $? = 0 ] && execute java Sudoku -g 04 -s -m NAKED_TRIPLE -v -d < Sudoku.0012
		[ $? = 0 ] && execute java Sudoku -g 41 -s -m NAKED_TRIPLE -v -d < Sudoku.0013
		# --method HIDDEN_SINGLE
		# [ $? = 0 ] && execute java Sudoku -g 00 -s -m HIDDEN_SINGLE -v < Sudoku.0001
		# [ $? = 0 ] && execute java Sudoku -g 00 -s -m HIDDEN_SINGLE -v < Sudoku.0004
		# [ $? = 0 ] && execute java Sudoku -g 00 -s -m HIDDEN_SINGLE -v < Sudoku.0005
		# --solve --verbose
		VERBOSE=-v
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0012 # 42 -> solved
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0013 # 38 -> solved
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0011 # 37 -> 37
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0002 # 30 -> solved
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0003 # 27 -> solved
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0010 # 26 -> 27
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0006 # 24 -> 26
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0007 # 23 -> solved
		# [ $? = 0 ] && execute java Sudoku -s $VERBOSE < Sudoku.0008 # 18 -> 21
	else
		echo ===== DEMO =====
		execute java Sudoku --help
		execute java Sudoku --interactive
		execute java Sudoku --coordinate SUDOKU < Sudoku.0002
		execute java Sudoku --analyze < Sudoku.0002
		execute java Sudoku --solve < Sudoku.0002
		execute java Sudoku -s --verbose < Sudoku.0002
		execute java Sudoku -s --go 41 --debug < Sudoku.0006
		execute java Sudoku -s --method NAKED_SINGLE < Sudoku.0000
		execute java Sudoku -s -m NAKED_SINGLE -m FULL_HOUSE < Sudoku.0001
	fi
fi
