package org.apache.commons.lang3;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SerializationUtils}.
 */
public class SerializationUtilsTest {

    // Helper class for serialization tests
    static class TestClass implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int value;

        TestClass(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            TestClass other = (TestClass) obj;
            return value == other.value;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    // Helper broken OutputStream to trigger IOException
    static class BrokenOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            throw new IOException("Simulated write error");
        }
    }

    // Helper broken InputStream to trigger IOException
    static class BrokenInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            throw new IOException("Simulated read error");
        }
    }

    // Tests constructor instantiation
    @Test
    public void testConstructor_default_createsInstance() {
        SerializationUtils utils = new SerializationUtils();
        assertNotNull(utils);
    }

    // Tests clone with valid object
    @Test
    public void testClone_validObject_returnsClonedObject() {
        TestClass original = new TestClass(42);
        TestClass cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(42, cloned.getValue());
    }

    // Tests clone with list collection
    @Test
    public void testClone_listObject_returnsDeepCopy() {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");

        List<String> cloned = SerializationUtils.clone((ArrayList<String>) list);
        assertNotNull(cloned);
        assertNotSame(list, cloned);
        assertEquals(list, cloned);
    }

    // Tests clone with null input
    @Test
    public void testClone_nullInput_returnsNull() {
        Object result = SerializationUtils.clone(null);
        assertNull(result);
    }

    // Tests clone with primitive class array to verify classloader primitive resolution (Defects4J Lang-13)
    @Test
    public void testClone_primitiveClasses_returnsClonedClasses() {
        Class<?>[] original = new Class<?>[] {
            byte.class, short.class, int.class, long.class,
            float.class, double.class, boolean.class, char.class, void.class
        };
        Class<?>[] cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original.length, cloned.length);
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], cloned[i]);
        }
    }

    // Tests serialize to byte array and deserialize from byte array
    @Test
    public void testSerializeAndDeserialize_validObject_returnsEqualObject() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        byte[] bytes = SerializationUtils.serialize((Serializable) map);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        @SuppressWarnings("unchecked")
        Map<String, String> deserialized = (Map<String, String>) SerializationUtils.deserialize(bytes);
        assertNotNull(deserialized);
        assertNotSame(map, deserialized);
        assertEquals(map, deserialized);
    }

    // Tests serialize with null object to byte array
    @Test
    public void testSerialize_nullObject_returnsSerializedNull() {
        byte[] bytes = SerializationUtils.serialize((Serializable) null);
        assertNotNull(bytes);

        Object result = SerializationUtils.deserialize(bytes);
        assertNull(result);
    }

    // Tests serialize with null OutputStream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSerialize_nullOutputStream_throwsIllegalArgumentException() {
        SerializationUtils.serialize("test", null);
    }

    // Tests serialize with broken OutputStream throws SerializationException
    @Test(expected = SerializationException.class)
    public void testSerialize_brokenOutputStream_throwsSerializationException() {
        SerializationUtils.serialize("test", new BrokenOutputStream());
    }

    // Tests serialize with valid OutputStream
    @Test
    public void testSerialize_validOutputStream_writesSuccessfully() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize("testString", baos);

        byte[] bytes = baos.toByteArray();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        Object result = SerializationUtils.deserialize(bytes);
        assertEquals("testString", result);
    }

    // Tests deserialize with null byte array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDeserialize_nullByteArray_throwsIllegalArgumentException() {
        SerializationUtils.deserialize((byte[]) null);
    }

    // Tests deserialize with null InputStream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDeserialize_nullInputStream_throwsIllegalArgumentException() {
        SerializationUtils.deserialize((InputStream) null);
    }

    // Tests deserialize with corrupted byte array throws SerializationException
    @Test(expected = SerializationException.class)
    public void testDeserialize_corruptedByteArray_throwsSerializationException() {
        byte[] corruptedData = new byte[] { 0, 1, 2, 3, 4, 5 };
        SerializationUtils.deserialize(corruptedData);
    }

    // Tests deserialize with broken InputStream throws SerializationException
    @Test(expected = SerializationException.class)
    public void testDeserialize_brokenInputStream_throwsSerializationException() {
        SerializationUtils.deserialize(new BrokenInputStream());
    }

    // Tests deserialize with valid InputStream
    @Test
    public void testDeserialize_validInputStream_returnsObject() {
        byte[] bytes = SerializationUtils.serialize("hello world");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        Object result = SerializationUtils.deserialize(bais);
        assertEquals("hello world", result);
    }
}