/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package cn.ussshenzhou.madparticle.util;

import org.lwjgl.system.Configuration;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * This class provides functionality for managing native memory.
 *
 * <p>All methods in this class will make use of {@link sun.misc.Unsafe} if it's available, for performance. If Unsafe is not available, the fallback
 * implementations make use of reflection and, in the worst-case, JNI.</p>
 *
 * <p>Method names in this class are prefixed with {@code mem} to avoid ambiguities when used with static imports.</p>
 *
 * <h3>Text encoding/decoding</h3>
 *
 * <p>Three codecs are available, each with a different postfix:</p>
 * <ul>
 * <li>UTF16 - Direct mapping of 2 bytes to Java char and vice versa</li>
 * <li>UTF8 - custom UTF-8 codec without intermediate allocations</li>
 * <li>ASCII - Not the original 7bit ASCII, but any character set with a single byte encoding (ISO 8859-1, Windows-1252, etc.)</li>
 * </ul>
 *
 * <p>The codec implementations do no codepoint validation, for improved performance. Therefore, if malformed input or unmappable characters are expected, the
 * JDK {@link CharsetEncoder}/{@link CharsetDecoder} classes should be used instead. Methods in bindings that accept/return {@code CharSequence}/{@code String}
 * also support {@code ByteBuffer}, so custom codecs can be used if necessary.</p>
 *
 * @see Configuration#MEMORY_ALLOCATOR
 * @see Configuration#DEBUG_MEMORY_ALLOCATOR
 */
@SuppressWarnings("removal")
public class MemoryUtil {

    static final sun.misc.Unsafe UNSAFE = getUnsafeInstance();

    private static sun.misc.Unsafe getUnsafeInstance() {
        java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();

        /*
        Different runtimes use different names for the Unsafe singleton,
        so we cannot use .getDeclaredField and we scan instead. For example:

        Oracle: theUnsafe
        PERC : m_unsafe_instance
        Android: THE_ONE
        */
        for (java.lang.reflect.Field field : fields) {
            if (!field.getType().equals(sun.misc.Unsafe.class)) {
                continue;
            }

            int modifiers = field.getModifiers();
            if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers))) {
                continue;
            }

            try {
                field.setAccessible(true);
                return (sun.misc.Unsafe) field.get(null);
            } catch (Exception ignored) {
            }
            break;
        }

        throw new UnsupportedOperationException("LWJGL requires sun.misc.Unsafe to be available.");
    }

    public static void memPutByte(long ptr, byte value) {
        UNSAFE.putByte(null, ptr, value);
    }

    public static void memPutShort(long ptr, short value) {
        UNSAFE.putShort(null, ptr, value);
    }

    public static void memPutInt(long ptr, int value) {
        UNSAFE.putInt(null, ptr, value);
    }

    public static void memPutLong(long ptr, long value) {
        UNSAFE.putLong(null, ptr, value);
    }

    public static void memPutFloat(long ptr, float value) {
        UNSAFE.putFloat(null, ptr, value);
    }

    public static void memPutDouble(long ptr, double value) {
        UNSAFE.putDouble(null, ptr, value);
    }
}
