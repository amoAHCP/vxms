package org.jacpfx.common.util;

import java.io.*;

/**
 * Created by amo on 04.12.14.
 */
public class Serializer {
    public static byte[] serialize(Object obj) throws IOException {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        o.close();
        return b.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        final ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }
}