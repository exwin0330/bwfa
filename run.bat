@echo off
REM 引数が渡されているかを確認
if "%1"=="" (
    echo Usage: run.bat HelloSoot
    exit /b
)

REM 引数に基づいてファイル名を設定
set "filename=%~1"

set filepath=""
set classpath=""

REM 有効なファイル名のチェック
if "%1"=="javac" (
    if "%2"=="" (
        echo select java file path
        exit /b
    )
    set "javapath=%~2"
    "c:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" -encoding utf-8 -d bin %javapath%
) else if "%1"=="HelloSoot" (
    set filepath="src\dfa\tutorial\HelloSoot\HelloSoot.java"
    set classpath="dfa.tutorial.HelloSoot.HelloSoot"
) else if "%1"=="flow" (
    set filepath="src\dfa\tutorial\flow\SimpleBackwardFlowAnalysis.java"
    set classpath="dfa.tutorial.flow.SimpleBackwardFlowAnalysis"
) else if "%1"=="multiMethodflow" (
    set filepath="src\dfa\tutorial\flow\MultiMethodBackwardFlowAnalysis.java"
    set classpath="dfa.tutorial.flow.MultiMethodBackwardFlowAnalysis"
) else if "%1"=="multiClassflow" (
    set filepath="src\dfa\tutorial\flow\MultiClassBackwardFlowAnalysis.java"
    set classpath="dfa.tutorial.flow.MultiClassBackwardFlowAnalysis"
) else if "%1"=="external" (
    set filepath="src\dfa\tutorial\flow\ExternalMethodDetector.java"
    set classpath="dfa.tutorial.flow.ExternalMethodDetector"
) else if "%1"=="methods" (
    set filepath="src\dfa\tutorial\flow\MethodDependencyAnalysis.java"
    set classpath="dfa.tutorial.flow.MethodDependencyAnalysis"
) else if "%1"=="backward" (
    set "filepath=src\dfa\tutorial\backward\BackwardSlicingExample.java src\dfa\tutorial\backward\BackwardSlicingAnalysis.java"
    set classpath="dfa.tutorial.backward.BackwardSlicingExample"
) else if "%1"=="cg" (
    set filepath="src\dfa\tutorial\flow\CallGraphAnalysis.java"
    set classpath="dfa.tutorial.flow.CallGraphAnalysis"
) else if "%1"=="mc" (
    set "filepath=src\dfa\tutorial\flow\MethodCallAnalysis.java src\dfa\tutorial\flow\AnalysisGraph.java"
    set classpath="dfa.tutorial.flow.MethodCallAnalysis"
) else if "%1"=="var" (
    set filepath="src\dfa\tutorial\backward\VariableFlowAnalysis.java"
    set classpath="dfa.tutorial.backward.VariableFlowAnalysis"
) else if "%1"=="rvar" (
    set "filepath=src\dfa\tutorial\backward\RecursiveVariableFlowAnalysis.java src\dfa\tutorial\backward\VariableFlowAnalysis.java"
    set classpath="dfa.tutorial.backward.RecursiveVariableFlowAnalysis"
) else if "%1"=="arg" (
    set filepath="src\dfa\tutorial\backward\ArgumentFlowAnalysis.java"
    set classpath="dfa.tutorial.backward.ArgumentFlowAnalysis"
) else (
    echo %filename% is invalid filename. Please use one of the following:
    type valid_filenames.txt
    exit /b
)

REM 指定されたJavaファイルをコンパイル
echo Compiling %filepath%...
"c:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" -encoding utf-8 -cp lib\soot-4.5.0-jar-with-dependencies.jar -d bin %filepath%
if %errorlevel% neq 0 (
    echo Failed to compile %filename%.java.
    exit /b
)
echo Running %filename%...
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
"c:\Program Files\Java\jdk1.8.0_202\bin\java.exe" -cp bin;lib\soot-4.5.0-jar-with-dependencies.jar %classpath%

exit /b