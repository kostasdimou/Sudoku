# Sudoku.java by Kostas Dimou @ 2019

Usage:

    Sudoku [--analyze] [--coordinates FORMAT] [--debug] [--file INPUT_FILE] \
           [--go YX] [--help] [--interactive] [--method METHOD] [--solve] \
           [--verbose] [--width WIDTH]

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
    -f or --file:
        Defines an external file for the initial population.
        Example for reading file Sudoku.9x9.0002:
            -f Sudoku.9x9.0002
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
        Example for file Sudoku.9x9.0002:
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
            HIDDEN_PAIR
            HIDDEN_TRIPLE
            HIDDEN_QUAD
            HIDDEN_QUINT
        Example for activating the method NAKED_SINGLE:
            -m NAKED_SINGLE
    -s or --solve:
        Solves the Sudoku by using all possible methods.
    -v or --verbose:
        Displays the Sudoku matrix on every passage.
    -w or --width:
        Defines the width of the Sudoku matrix.
        The accepted numbers should have an integer root (e.g. 4, 9, 16, ...).
        The default width is 9.
        Example for setting the width equal to 16:
            -w 16

Examples:

    java Sudoku --help
    java Sudoku --interactive
    java Sudoku --coordinate SUDOKU < Sudoku.9x9.0002
    java Sudoku --analyze < Sudoku.9x9.0002
    java Sudoku --solve < Sudoku.9x9.0002
    java Sudoku -s --verbose < Sudoku.9x9.0002
    java Sudoku -s --go 41 --debug < Sudoku.9x9.0006
    java Sudoku -s --method NAKED_SINGLE < Sudoku.9x9.0000
    java Sudoku -s -m FULL_HOUSE -m NAKED_SINGLE < Sudoku.9x9.0001
