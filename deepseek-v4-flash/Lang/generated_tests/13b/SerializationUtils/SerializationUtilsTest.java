package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializationUtilsTest {

    // Tests null input to clone returns null
    @Test
    public void testClone_nullInput_returnsNull() {
        assertNull(SerializationUtils.clone(null));
    }

    // Tests cloning a simple serializable object returns equal but distinct object
    @Test
    public void testClone_serializableString_returnsEqualObject() {
        String original = "hello";
        String cloned = SerializationUtils.clone(original);
        assertNotNull(cloned);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    // Tests cloning an ArrayList copies contents
    @Test
    public void testClone_serializableList_returnsEqualList() {
        ArrayList<String> original = new ArrayList<String>();
        original.add("a");
        original.add("b");
        ArrayList<String> cloned = SerializationUtils.clone(original);
        assertNotNull(cloned);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    // Tests serialize with null OutputStream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSerialize_nullOutputStream_throwsIllegalArgumentException() {
        SerializationUtils.serialize("data", null);
    }

    // Tests serialize to stream writes at least one byte
    @Test
    public void testSerialize_validObject_writesToStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize("data", baos);
        assertTrue(baos.size() > 0);
    }

    // Tests serializing null object returns byte array that deserializes to null
    @Test
    public void testSerialize_nullObject_returnsByteArray() {
        byte[] data = SerializationUtils.serialize((Serializable) null);
        assertNotNull(data);
        assertNull(SerializationUtils.deserialize(data));
    }

    // Tests deserialize with null InputStream throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDeserialize_nullInputStream_throwsIllegalArgumentException() {
        SerializationUtils.deserialize((InputStream) null);
    }

    // Tests deserialize with null byte array throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testDeserialize_nullByteArray_throwsIllegalArgumentException() {
        SerializationUtils.deserialize((byte[]) null);
    }

    // Tests deserialize of an empty byte array throws SerializationException
    @Test(expected = SerializationException.class)
    public void testDeserialize_emptyByteArray_throwsSerializationException() {
        SerializationUtils.deserialize(new byte[0]);
    }

    // Tests deserialize of invalid stream data throws SerializationException
    @Test(expected = SerializationException.class)
    public void testDeserialize_invalidStream_throwsSerializationException() {
        byte[] invalid = {0, 1, 2, 3, 4, 5};
        SerializationUtils.deserialize(invalid);
    }

    // Tests serialize of an object containing a non-serializable field throws SerializationException
    @Test(expected = SerializationException.class)
    public void testSerialize_nonSerializableField_throwsSerializationException() {
        NonSerializableField obj = new NonSerializableField();
        SerializationUtils.serialize(obj);
    }

    // Tests clone of an object containing a non-serializable field throws SerializationException
    @Test(expected = SerializationException.class)
    public void testClone_nonSerializableField_throwsSerializationException() {
        NonSerializableField obj = new NonSerializableField();
        SerializationUtils.clone(obj);
    }

    // Tests round-trip serialization/deserialization preserves object state
    @Test
    public void testSerialize_deserialize_roundTripObject() {
        TestObject original = new TestObject("value", 42);
        byte[] data = SerializationUtils.serialize(original);
        TestObject result = (TestObject) SerializationUtils.deserialize(data);
        assertNotNull(result);
        assertEquals(original.getStringValue(), result.getStringValue());
        assertEquals(original.getIntValue(), result.getIntValue());
        assertNotSame(original, result);
    }

    // Tests clone of a custom serializable object returns equal but distinct object
    @Test
    public void testClone_customObject_returnsEqualObject() {
        TestObject original = new TestObject("x", 123);
        TestObject cloned = SerializationUtils.clone(original);
        assertNotNull(cloned);
        assertEquals(original.getStringValue(), cloned.getStringValue());
        assertEquals(original.getIntValue(), cloned.getIntValue());
        assertNotSame(original, cloned);
    }

    // Tests public constructor can be instantiated
    @Test
    public void testConstructor_instantiable() {
        assertNotNull(new SerializationUtils());
    }

    // Helper serializable class
    private static class TestObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private String stringValue;
        private int intValue;

        TestObject(String s, int i) {
            stringValue = s;
            intValue = i;
        }

        String getStringValue() {
            return stringValue;
        }

        int getIntValue() {
            return intValue;
        }
    }

    // Helper class with a non-serializable field
    private static class NonSerializableField implements Serializable {
        private static final long serialVersionUID = 1L;
        private Object nonSerializable = new Object();
    }
}