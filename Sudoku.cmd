@cls
@set JAVA=Sudoku.java
@set CLASS=Sudoku.class
@set COMPILER_OPTIONS="-Xmaxerrs 10 -Xmaxwarns 10"
@for /f %%f in ('dir /b /o:d "%JAVA%" "%CLASS%"') do @set NEWEST=%%f
@if %NEWEST% == %JAVA% (
	@echo ===== COMPILING =====
	@echo javac %COMPILER_OPTIONS% %JAVA%
	javac %COMPILER_OPTIONS% %JAVA%
)
@if %ERRORLEVEL% == 0 (
	@if "%~1" == "" (
		@echo ===== TEST =====
		@rem if %ERRORLEVEL% == 0 java Sudoku -s -g 001 -m NAKED_SINGLE < Sudoku.9x9.0000 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s -g 001 -m HIDDEN_SINGLE < Sudoku.9x9.0001 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s -g 001 -m HIDDEN_SINGLE < Sudoku.9x9.0004 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s -g 001 -m HIDDEN_SINGLE < Sudoku.9x9.0005 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -d -v -g 419 -m HIDDEN_SINGLE < Sudoku.9x9.0006 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s < Sudoku.9x9.0002 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s < Sudoku.9x9.0003 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s < Sudoku.9x9.0006 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -s < Sudoku.9x9.0007 | more 
		@rem if %ERRORLEVEL% == 0 java Sudoku -d -s -g 201 < Sudoku.9x9.0002 | more 
		@if %ERRORLEVEL% == 0 java Sudoku -d -a -s < Sudoku.9x9.0002 | more 
	) else (
		@echo ===== DEMO =====
		java Sudoku --help
		pause
		java Sudoku --interactive
		pause
		java Sudoku --coordinate SUDOKU < Sudoku.9x9.0002
		pause
		java Sudoku --analyze < Sudoku.9x9.0002
		pause
		java Sudoku --solve < Sudoku.9x9.0002
		pause
		java Sudoku -s --verbose < Sudoku.9x9.0002
		pause
		java Sudoku -s --go 419 --debug < Sudoku.9x9.0006
		pause
		java Sudoku -s --method NAKED_SINGLE < Sudoku.9x9.0000
		pause
		java Sudoku -s -m NAKED_SINGLE -m FULL_HOUSE < Sudoku.9x9.0001
	)
)
