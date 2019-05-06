# Sudoku.java by Kostas Dimou @ 2019

Usage:

    Sudoku [--analyze] [--coordinates SYSTEM] [--debug] [--go YXN] [--help] \
           [--interactive] [--method METHOD] [--solve] [--view]

Where:

    -a or --analyze:
        Displays the posible positions for each number.
    -c or --coordinates:
        Selects the coordinates system for the Sudoku matrix.
        Available systems:
            CHESS:  Y=9-1, X=A-I
            JAVA:   Y=0-9, X=0-9 (default)
            SUDOKU: Y=A-I, X=1-9
    -d or --debug:
        Activates the debugging mode.
    -g or --go:
        Try a specific coordinate and/or a specific number.
        Example:
            --go 419: row = 4, column = 1, number = 9.
            --chess --go 5A9: row = 4, column = 1, number = 9.
    -h or --help:
        Displays this help message and exits.
    -i or --interactive:
        Displays a prompt for each imput row and pauses on each passage.
        For each missing number you can provide a zero (0) or a space ( ).
        Example:
            ROW[0] = 57 9  1
            ROW[1] = 01030005
            ROW[3] =  2070 0 9
    -m or --method:
        Calls the equivalent method for solving the Sudoku.
        By default all methods are called.
        Available methods:
            HIDDEN_SINGLE
            MISSING
            NAKED_SINGLE
    -s or --solve:
        Solves the Sudoku by using all possible methods.
    -v or --view:
        Displays the Sudoku matrix on every passage.

Examples:

    java Sudoku --help
    java Sudoku --interactive
    java Sudoku < Sudoku.0002
    java Sudoku --analyze < Sudoku.0002
    java Sudoku --solve < Sudoku.0002
    java Sudoku -s --view < Sudoku.0002
    java Sudoku -s --go 419 --debug < Sudoku.0006
    java Sudoku -s --method NAKED_SINGLE < Sudoku.0000
    java Sudoku -s -m NAKED_SINGLE -m MISSING < Sudoku.0001
