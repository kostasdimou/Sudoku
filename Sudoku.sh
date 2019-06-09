#!/bin/bash
clear
# ------------------------------------------------------------------------------
function execute() {
	echo "$@"
	$@
	ERRNO=$?
	[ $PAUSE = 1 ] && pause
} # function pause()
# ------------------------------------------------------------------------------
function pause() {
	echo
	read -p "Press Enter to continue" X
	echo
} # function pause()
# ------------------------------------------------------------------------------
function check() {
	execute $@
} # function check()
# ------------------------------------------------------------------------------
echo ===== COMPILING =====
ERRNO=0
PAUSE=0
for JAVA in *.java
do
	CLASS=${JAVA%.java}.class
	if [ $JAVA -nt $CLASS ]
	then
		execute javac $COMPILER_OPTIONS $JAVA
		[ $ERRNO != 0 ] && break
	fi
done
PAUSE=1
if [ $ERRNO == 0 ]
then
	if [ $# == 0 ]
	then
		echo ===== TESTING =====
		# --coordinate
		# [ $ERRNO = 0 ] && execute java Sudoku -c CHESS  -f Sudoku.9x9.0002
		# [ $ERRNO = 0 ] && execute java Sudoku -c JAVA   -f Sudoku.9x9.0002
		# [ $ERRNO = 0 ] && execute java Sudoku -c ROWCOL -f Sudoku.9x9.0002
		# [ $ERRNO = 0 ] && execute java Sudoku -c SUDOKU -f Sudoku.9x9.0002
		# --go
		# [ $ERRNO = 0 ] && execute java Sudoku -c CHESS  -g 9a   -f Sudoku.9x9.0000
		# [ $ERRNO = 0 ] && execute java Sudoku -c JAVA   -g 00   -f Sudoku.9x9.0000
		# [ $ERRNO = 0 ] && execute java Sudoku -c ROWCOL -g r1c1 -f Sudoku.9x9.0000
		# [ $ERRNO = 0 ] && execute java Sudoku -c SUDOKU -g a1   -f Sudoku.9x9.0000
		# --method FULL_HOUSE
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 04 -g 38 -g 53 -s -m FULL_HOUSE -f Sudoku.9x9.0008
		# --method NAKED_SINGLE
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 00 -s -m NAKED_SINGLE -f Sudoku.9x9.0000
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 14 -s -m NAKED_SINGLE -f Sudoku.9x9.0009
		# --method NAKED_PAIR
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 50 -s -m NAKED_PAIR -f Sudoku.9x9.0010
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 26 -s -m NAKED_PAIR -f Sudoku.9x9.0011
		# --method NAKED_TRIPLE
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 04 -s -m NAKED_TRIPLE -f Sudoku.9x9.0012
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 41 -s -m NAKED_TRIPLE -f Sudoku.9x9.0013
		# --method NAKED_QUAD
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 70 -s -m NAKED_QUAD -f Sudoku.9x9.0014
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 62 -s -m NAKED_QUAD -f Sudoku.9x9.0015
		# --method HIDDEN_SINGLE
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 00 -s -m HIDDEN_SINGLE -v -f Sudoku.9x9.0001
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 00 -s -m HIDDEN_SINGLE -v -f Sudoku.9x9.0004
		# [ $ERRNO = 0 ] && execute java Sudoku -v -g 00 -s -m HIDDEN_SINGLE -v -f Sudoku.9x9.0005
		# --method HIDDEN_PAIR
		# [ $ERRNO = 0 ] && execute java Sudoku -d -v -g 50 -s -m HIDDEN_PAIR -v -f Sudoku.9x9.0010
		# [ $ERRNO = 0 ] && execute java Sudoku -d -v -g 48 -s -m HIDDEN_PAIR -v -f Sudoku.9x9.0017
		# --method HIDDEN_TRIPLE
		# [ $ERRNO = 0 ] && execute java Sudoku -d -v -g 41 -s -m HIDDEN_TRIPLE -v -f Sudoku.9x9.0013
		# --method HIDDEN_QUAD
		# [ $ERRNO = 0 ] && execute java Sudoku -d -v -g 08 -s -m HIDDEN_QUAD -v -f Sudoku.9x9.0016
		# --solve --verbose
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0013 # 58 -> 81
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0017 # 54 -> 54
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0014 # 50 -> 81
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0012 # 42 -> 81
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0015 # 41 -> 81
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0011 # 37 -> 37
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0002 # 30 -> 77
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0016 # 32 -> 32
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0003 # 27 -> 81
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0010 # 26 -> 27
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0006 # 24 -> 26
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0007 # 23 -> 81
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0008 # 18 -> 21
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0005 #  9 -> 10
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0000 #  8 ->  9
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0009 #  8 ->  9
		# [ $ERRNO = 0 ] && execute java Sudoku -s -v -f Sudoku.9x9.0001 #  4 ->  5
		# SOLVED
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0013 # 58 -> 81
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0014 # 50 -> 81
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0012 # 42 -> 81
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0015 # 41 -> 81
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0002 # 30 -> 81
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0003 # 27 -> 81
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0006 # 24 -> 81
		# UNSOLVED
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0017 # 54 -> 54
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0011 # 37 -> 37
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0016 # 32 -> 32
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0007 # 23 -> 29
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0010 # 26 -> 33
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0008 # 18 -> 21
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0005 #  9 -> 10
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0000 #  8 ->  9
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0009 #  8 ->  9
		[ $ERRNO = 0 ] && execute java Sudoku -s -f Sudoku.9x9.0001 #  4 ->  5
	else
		echo ===== DEMO =====
		execute java Sudoku --help
		execute java Sudoku --interactive
		execute java Sudoku --coordinate SUDOKU --file Sudoku.9x9.0002
		execute java Sudoku --analyze --file Sudoku.9x9.0002
		execute java Sudoku --solve --file Sudoku.9x9.0002
		execute java Sudoku --solve --verbose --file Sudoku.9x9.0002
		execute java Sudoku --solve --go 41 --debug --file Sudoku.9x9.0006
		execute java Sudoku --solve --method NAKED_SINGLE --file Sudoku.9x9.0000
		execute java Sudoku --solve --method NAKED_SINGLE --method FULL_HOUSE --file Sudoku.9x9.0001
	fi
fi
