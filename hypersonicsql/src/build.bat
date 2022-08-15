rem If you don't have jikes replace 'jikes' with 'javac' on the next line
javac *.java org\hsql\*.java org\hsql\util\*.java

jar -cf ..\demo\hsql.jar *.class .\org\hsql\*.class .\org\hsql\util\*.class
del *.class
del org\hsql\*.class
del org\hsql\util\*.class
