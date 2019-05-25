# Sudoku.java by Kostas Dimou @ 2019

Usage:

    Sudoku [--analyze] [--coordinates FORMAT] [--debug] [--go YX] [--help] \
           [--interactive] [--method METHOD] [--solve] [--verbose]

Where:

    -a or --analyze:
        Displays the posible positions for each number.
    -c or --coordinates:
        Selects the coordinates format for the Sudoku matrix.
        Available formats:
            CHESS:  Y=9..1,   X=A..I
            JAVA:   Y=0..9,   X=0..9 (default)
            ROWCOL: Y=R1..R9, X=C1..C9
            SUDOKU: Y=A..I,   X=1..9
    -d or --debug:
        Activates the debugging mode.
    -g or --go:
        Check a specific coordinate.
        Example for row = 4, column = 0:
            -c CHESS  -g 5A
            -c JAVA   -g 40
            -c ROWCOL -g R5C1
            -c SUDOKU -g D1
    -h or --help:
        Displays this help message and exits.
    -i or --interactive:
        Displays a prompt for each imput row and pauses on each passage.
        For each missing number you can provide a zero (0) or a space ( ).
        Example for file Sudoku.0002:
            ROW[0] = 53  7
            ROW[1] = 6  195
            ROW[2] =  98    6
            ROW[3] = 8   6   3
            ROW[4] = 4  8 3  1
            ROW[5] = 7   2   6
            ROW[6] =  6    28
            ROW[7] =    419  5
            ROW[8] =     8  79
    -m or --method:
        Calls the equivalent method for solving the Sudoku.
        By default all methods are called.
        Available methods:
            FULL_HOUSE
            NAKED_SINGLE
            NAKED_PAIR
            NAKED_TRIPLE
            NAKED_QUAD
            NAKED_QUINT
            HIDDEN_SINGLE
    -s or --solve:
        Solves the Sudoku by using all possible methods.
    -v or --verbose:
        Displays the Sudoku matrix on every passage.

Examples:

    java Sudoku --help
    java Sudoku --interactive
    java Sudoku --coordinate SUDOKU < Sudoku.0002
    java Sudoku --analyze < Sudoku.0002
    java Sudoku --solve < Sudoku.0002
    java Sudoku -s --verbose < Sudoku.0002
    java Sudoku -s --go 41 --debug < Sudoku.0006
    java Sudoku -s --method NAKED_SINGLE < Sudoku.0000
    java Sudoku -s -m FULL_HOUSE -m NAKED_SINGLE < Sudoku.0001
