package org.apache.commons.lang3;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SerializationUtilsTest {

    // Helper class for serialization tests
    private static class DummySerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        private int value;

        public DummySerializable(int value) {
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
            DummySerializable that = (DummySerializable) obj;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    // Helper class with non-serializable field to trigger SerializationException
    private static class NonSerializableFieldClass implements Serializable {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private final Object nonSerializable = new Object();
    }

    // Tests constructor
    @Test
    public void testConstructor_default_instanceCreated() {
        SerializationUtils utils = new SerializationUtils();
        assertNotNull(utils);
    }

    // Tests clone with null input
    @Test
    public void testClone_nullInput_returnsNull() {
        assertNull(SerializationUtils.clone(null));
    }

    // Tests clone with standard object
    @Test
    public void testClone_validObject_returnsClonedCopy() {
        DummySerializable original = new DummySerializable(42);
        DummySerializable cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(42, cloned.getValue());
    }

    // Tests clone with standard library collection
    @Test
    public void testClone_mapObject_returnsClonedMap() {
        HashMap<String, String> original = new HashMap<String, String>();
        original.put("key1", "value1");
        original.put("key2", "value2");

        HashMap<String, String> cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
    }

    // Tests clone with primitive array (regression test for primitive class resolution)
    @Test
    public void testClone_primitiveByteArray_returnsClonedArray() {
        byte[] original = new byte[]{1, 2, 3, 4, 5};
        byte[] cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    // Tests clone with primitive int array
    @Test
    public void testClone_primitiveIntArray_returnsClonedArray() {
        int[] original = new int[]{10, 20, 30};
        int[] cloned = SerializationUtils.clone(original);

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    // Tests clone failure with non-serializable field
    @Test(expected = SerializationException.class)
    public void testClone_unserializableField_throwsSerializationException() {
        SerializationUtils.clone(new NonSerializableFieldClass());
    }

    // Tests serialize to byte array and deserialize from byte array
    @Test
    public void testSerializeAndDeserialize_validObject_returnsEqualObject() {
        DummySerializable original = new DummySerializable(100);
        byte[] bytes = SerializationUtils.serialize(original);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        Object deserialized = SerializationUtils.deserialize(bytes);
        assertNotNull(deserialized);
        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
    }

    // Tests serialize to byte array with null object
    @Test
    public void testSerialize_nullObject_returnsByteArray() {
        byte[] bytes = SerializationUtils.serialize(null);
        assertNotNull(bytes);

        Object deserialized = SerializationUtils.deserialize(bytes);
        assertNull(deserialized);
    }

    // Tests serialize to stream with null stream throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSerialize_nullOutputStream_throwsIllegalArgumentException() {
        SerializationUtils.serialize(new DummySerializable(1), null);
    }

    // Tests serialize to stream with valid stream
    @Test
    public void testSerialize_validOutputStream_writesData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(new DummySerializable(5), baos);
        byte[] bytes = baos.toByteArray();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    // Tests serialize to stream failure
    @Test(expected = SerializationException.class)
    public void testSerialize_unserializableObject_throwsSerializationException() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(new NonSerializableFieldClass(), baos);
    }

    // Tests deserialize from stream with null stream throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testDeserialize_nullInputStream_throwsIllegalArgumentException() {
        SerializationUtils.deserialize((InputStream) null);
    }

    // Tests deserialize from byte array with null array throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testDeserialize_nullByteArray_throwsIllegalArgumentException() {
        SerializationUtils.deserialize((byte[]) null);
    }

    // Tests deserialize from stream with valid input
    @Test
    public void testDeserialize_validInputStream_returnsDeserializedObject() {
        DummySerializable original = new DummySerializable(77);
        byte[] bytes = SerializationUtils.serialize(original);

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Object result = SerializationUtils.deserialize(bais);

        assertNotNull(result);
        assertEquals(original, result);
    }

    // Tests deserialize with invalid/corrupt stream throws exception
    @Test(expected = SerializationException.class)
    public void testDeserialize_corruptedInputStream_throwsSerializationException() {
        byte[] invalidData = new byte[]{0, 1, 2, 3, 4};
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidData);
        SerializationUtils.deserialize(bais);
    }

    // Tests deserialize with invalid/corrupt byte array throws exception
    @Test(expected = SerializationException.class)
    public void testDeserialize_corruptedByteArray_throwsSerializationException() {
        byte[] invalidData = new byte[]{0, 1, 2, 3, 4};
        SerializationUtils.deserialize(invalidData);
    }
}