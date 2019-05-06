#!/bin/bash
clear
PROGRAM=Sudoku
JAVA=$PROGRAM.java
CLASS=$PROGRAM.class
# ------------------------------------------------------------------------------
function pause()
{
	read -p "Press Enter to continue" X
} # function pause()
# ------------------------------------------------------------------------------
if [ $JAVA -nt $CLASS ]
then
	echo ===== COMPILING =====
	echo javac $JAVA
	javac $JAVA
else
	if [ $? = 0 ]
	then
		if [ $# == 0 ]
		then
			echo ===== TEST =====
			# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setSingle < $PROGRAM.0000 | more 
			# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setMissing < $PROGRAM.0001 | more 
			# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setMissing < $PROGRAM.0004 | more 
			# [ $? = 0 ] && java $PROGRAM -s -g 001 -m setMissing < $PROGRAM.0005 | more 
			# [ $? = 0 ] && java $PROGRAM -d -f -g 419 -m setAllowed < $PROGRAM.0006 | more 
			# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0002 | more 
			# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0003 | more 
			# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0006 | more 
			# [ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0007 | more 
			[ $? = 0 ] && java $PROGRAM -s < $PROGRAM.0002 | more 
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
fi
