@ECHO OFF
REM This is required in order to change the value of a variable and evaluate it using !var!
SETLOCAL ENABLEDELAYEDEXPANSION

REM #-------------------------------------------------------------------------------------------------------------------
REM # DataLoader alias script for running on the command line by simply using: $> dataloader <args>
REM #-------------------------------------------------------------------------------------------------------------------
REM #
REM # Will use a file like dataloader-<version>.java in the current directory if it exists, otherwise it will attempt
REM # to use the same file out of the target directory.  We need to match the ending digit in the target folder to avoid
REM # picking up the dataloader-<version>-javadoc.jar
REM #
REM # Pass all command line arguments through to the application ("%*")

SET found=0
FOR %%f IN (dataloader-*.jar) DO (
    IF !found! EQU 0 (
        java -jar %%f %*
        SET found=1
    )
)

IF !found! EQU 1 (
    GOTO :end
)

FOR %%f IN (target/dataloader-*.?.jar) DO (
    IF !found! EQU 0 (
        java -jar target/%%f %*
        SET found=1
    )
)

IF !found! EQU 0 (
    ECHO ERROR: Cannot find dataloader-x.y.z.jar file to execute
)

:end
