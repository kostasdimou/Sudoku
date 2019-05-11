#!/bin/bash
clear
PROGRAM=Sudoku
# ------------------------------------------------------------------------------
function pause()
{
	read -p "Press Enter to continue" X
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
		# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setSingle < $PROGRAM.0000
		# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setMissing < $PROGRAM.0001
		# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setMissing < $PROGRAM.0004
		# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setMissing < $PROGRAM.0005
		# [ $? = 0 ] && java $PROGRAM -d -f -g 419 -m setAllowed < $PROGRAM.0006
		# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0002
		# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0003
		# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0006
		# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0007
		[ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0002
	else
		echo ===== DEMO =====
		java $PROGRAM --help
		pause
		java $PROGRAM --interactive
		pause
		java $PROGRAM --coordinate internet < $PROGRAM.0002
		pause
		java $PROGRAM --analyze < $PROGRAM.0002
		pause
		java $PROGRAM --solve < $PROGRAM.0002
		pause
		java $PROGRAM -s --view < $PROGRAM.0002
		pause
		java $PROGRAM -s --go 419 --debug < $PROGRAM.0006
		pause
		java $PROGRAM -s --method setSingle < $PROGRAM.0000
		pause
		java $PROGRAM -s -m setSingle -m setMissing < $PROGRAM.0001
	fi
fi
