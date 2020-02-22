import org.graalvm.nativeimage.*;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.Pointer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Leak {
    public static void main(String[] args) throws IOException {
        BufferedReader obj = new BufferedReader(new InputStreamReader(System.in));
        String str;

        System.out.println("Enter n/i/s a space then the text to upper case; n=normal function call/i=with isolate/s=stop the program");
        do {
            str = obj.readLine();

            switch (str.charAt(0)) {
                case 'n':
                    System.out.println(new String(normal(str.substring(2).getBytes()), Charset.defaultCharset()));
                    break;
                case 'i':
                    System.out.println(new String(isolate(str.substring(2).getBytes()), Charset.defaultCharset()));
                    break;
                case 's':
                    return;
            }
        }   while(true);
    }

    private static byte[] normal(byte[] text){
        return new String(text, Charset.defaultCharset()).toUpperCase().getBytes();
    }

    private static byte[] isolate(byte[] text){
        IsolateThread newContext = Isolates.createIsolate(Isolates.CreateIsolateParameters.getDefault());
        IsolateThread parentContext = CurrentIsolate.getCurrentThread();
        ObjectHandle outputBufferHandle;
        try (PinnedObject pin = PinnedObject.create(text)) {
            outputBufferHandle = isolate(newContext, parentContext, pin.addressOfArrayElement(0), text.length);
        }

        ByteBuffer outputBuffer = ObjectHandles.getGlobal().get(outputBufferHandle);
        ObjectHandles.getGlobal().destroy(outputBufferHandle);
        Isolates.tearDownIsolate(newContext);

        return outputBuffer.array();
    }

    @CEntryPoint
    private static ObjectHandle isolate(@CEntryPoint.IsolateThreadContext IsolateThread newContext,
                                        IsolateThread parentContext, Pointer address, int length){
        ByteBuffer direct = CTypeConversion.asByteBuffer(address, length);
        ByteBuffer copy = ByteBuffer.allocate(length);
        copy.put(direct).rewind();
        byte[] outputBuffer = normal(copy.array());
        ObjectHandle outputBufferHandle;
        try (PinnedObject pin = PinnedObject.create(outputBuffer)) {
            outputBufferHandle = createByteBuffer(parentContext, pin.addressOfArrayElement(0), outputBuffer.length);
        }
        return outputBufferHandle;
    }

    @CEntryPoint
    private static ObjectHandle createByteBuffer(IsolateThread newContext, Pointer address, int length) {
        ByteBuffer direct = CTypeConversion.asByteBuffer(address, length);
        ByteBuffer copy = ByteBuffer.allocate(length);
        copy.put(direct).rewind();
        return ObjectHandles.getGlobal().create(copy);
    }
}
