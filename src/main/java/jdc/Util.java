package jdc;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;

/**
 * @author Jorge De Castro
 */
public final class Util {
    public static final Unsafe unsafe;

    static {
        try {
            Constructor<Unsafe> constructor = Unsafe.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            unsafe = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
