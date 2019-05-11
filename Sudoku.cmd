@cls
@set PROGRAM=Sudoku
@set JAVA=%PROGRAM%.java
@set CLASS=%PROGRAM%.class
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
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s -g 001 -m setSingle < %PROGRAM%.0000 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s -g 001 -m setMissing < %PROGRAM%.0001 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s -g 001 -m setMissing < %PROGRAM%.0004 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s -g 001 -m setMissing < %PROGRAM%.0005 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -d -f -g 419 -m setAllowed < %PROGRAM%.0006 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s < %PROGRAM%.0002 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s < %PROGRAM%.0003 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s < %PROGRAM%.0006 | more 
		@rem if %ERRORLEVEL% == 0 java %PROGRAM% -s < %PROGRAM%.0007 | more 
		@if %ERRORLEVEL% == 0 java %PROGRAM% -d -s -g 201 < %PROGRAM%.0002 | more 
	) else (
		@echo ===== DEMO =====
		java %PROGRAM% --help
		pause
		java %PROGRAM% --interactive
		pause
		java %PROGRAM% --coordinate internet < %PROGRAM%.0002
		pause
		java %PROGRAM% --analyze < %PROGRAM%.0002
		pause
		java %PROGRAM% --solve < %PROGRAM%.0002
		pause
		java %PROGRAM% -s --view < %PROGRAM%.0002
		pause
		java %PROGRAM% -s --go 419 --debug < %PROGRAM%.0006
		pause
		java %PROGRAM% -s --method setSingle < %PROGRAM%.0000
		pause
		java %PROGRAM% -s -m setSingle -m setMissing < %PROGRAM%.0001
	)
)
