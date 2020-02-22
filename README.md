# graal-windows-isolate-leak
 show a leak with graal isolates on windows

Compile with maven :

    >mvn package

Create a native image :
 
    >native-image -jar target\graal-windows-isolate-leak-1.0-SNAPSHOT-jar-with-dependencies.jar

Start the native exe :

    >graal-windows-isolate-leak-1.0-SNAPSHOT-jar-with-dependencies.exe
    
It will return the string you entered with upper case, with n as normal function call, i with an isolate, s to stop :
     
    Enter n/i/s a space then the text to upper case
    n normal
    NORMAL
    i isolate
    ISOLATE
    s
    
 We can see that each isolate call create 3.5MB of private working set physical memory, while none in the normal call.
 Only on windows, linux doesn't seems affected, tested with version of Graal CE 20.1.0-dev with java 11 and java 8 versions (using v8 of the sdk).