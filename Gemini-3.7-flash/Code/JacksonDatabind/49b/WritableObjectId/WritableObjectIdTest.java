package com.fasterxml.jackson.databind.ser.impl;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class WritableObjectIdTest
{
    private JsonFactory jsonFactory;
    private SerializerProvider serializerProvider;
    private JavaType stringType;

    @Before
    public void setUp() {
        jsonFactory = new JsonFactory();
        serializerProvider = new ObjectMapper().getSerializerProvider();
        stringType = TypeFactory.defaultInstance().constructType(String.class);
    }

    // Tests constructor field initialization
    @Test
    public void testConstructor_initializesFields() {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);

        assertSame(generator, woid.generator);
        assertNull(woid.id);
        assertFalse(woid.idWritten);
    }

    // Tests generating id when id is null
    @Test
    public void testGenerateId_whenIdIsNull_generatesNewId() {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);

        Object id = woid.generateId(new Object());

        assertEquals(1, id);
        assertEquals(1, woid.id);
    }

    // Tests that generateId returns existing id rather than generating a new one
    @Test
    public void testGenerateId_whenIdAlreadyGenerated_returnsExistingId() {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);

        Object firstId = woid.generateId(new Object());
        assertEquals(1, firstId);

        Object secondId = woid.generateId(new Object());
        assertEquals(1, secondId);
    }

    // Tests that generateId uses pre-assigned id
    @Test
    public void testGenerateId_withPreAssignedId_returnsExistingId() {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = 999;

        Object result = woid.generateId(new Object());

        assertEquals(999, result);
        assertEquals(999, woid.id);
    }

    // Tests writeAsId when id is null
    @Test
    public void testWriteAsId_whenIdIsNull_returnsFalse() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("id"), generator, ToStringSerializer.instance, true);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        boolean written = woid.writeAsId(gen, serializerProvider, writer);

        assertFalse(written);
    }

    // Tests writeAsId when id is not null but idWritten is false and alwaysAsId is false
    @Test
    public void testWriteAsId_whenIdNotNullAndNotWrittenAndAlwaysAsIdFalse_returnsFalse() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = "id123";
        woid.idWritten = false;
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("id"), generator, ToStringSerializer.instance, false);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        boolean written = woid.writeAsId(gen, serializerProvider, writer);

        assertFalse(written);
    }

    // Tests writeAsId when id is not null and idWritten is true
    @Test
    public void testWriteAsId_whenIdNotNullAndIdWrittenTrue_serializesId() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = "id123";
        woid.idWritten = true;
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("id"), generator, ToStringSerializer.instance, false);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        boolean written = woid.writeAsId(gen, serializerProvider, writer);
        gen.flush();

        assertTrue(written);
        assertEquals("\"id123\"", sw.toString());
    }

    // Tests writeAsId when alwaysAsId is true
    @Test
    public void testWriteAsId_whenIdNotNullAndAlwaysAsIdTrue_serializesId() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = "id123";
        woid.idWritten = false;
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("id"), generator, ToStringSerializer.instance, true);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        boolean written = woid.writeAsId(gen, serializerProvider, writer);
        gen.flush();

        assertTrue(written);
        assertEquals("\"id123\"", sw.toString());
    }

    // Tests writeAsId when generator supports native object ids
    @Test
    public void testWriteAsId_whenCanWriteObjectId_writesObjectRef() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = 123;
        woid.idWritten = true;
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("id"), generator, ToStringSerializer.instance, false);

        StringWriter sw = new StringWriter();
        NativeIdJsonGenerator gen = new NativeIdJsonGenerator(jsonFactory.createGenerator(sw));

        boolean written = woid.writeAsId(gen, serializerProvider, writer);

        assertTrue(written);
        assertEquals("123", gen.writtenRef);
    }

    // Tests writeAsField when generator supports native object ids
    @Test
    public void testWriteAsField_whenCanWriteObjectId_writesObjectId() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = 456;
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("id"), generator, ToStringSerializer.instance, false);

        StringWriter sw = new StringWriter();
        NativeIdJsonGenerator gen = new NativeIdJsonGenerator(jsonFactory.createGenerator(sw));

        woid.writeAsField(gen, serializerProvider, writer);

        assertTrue(woid.idWritten);
        assertEquals("456", gen.writtenId);
    }

    // Tests writeAsField when generator does not support native object ids and propertyName is provided
    @Test
    public void testWriteAsField_whenCannotWriteObjectIdAndPropertyNameNotNull_writesField() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = "val456";
        ObjectIdWriter writer = new ObjectIdWriter(stringType, new SerializedString("idProp"), generator, ToStringSerializer.instance, false);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        gen.writeStartObject();

        woid.writeAsField(gen, serializerProvider, writer);

        gen.writeEndObject();
        gen.flush();

        assertTrue(woid.idWritten);
        assertEquals("{\"idProp\":\"val456\"}", sw.toString());
    }

    // Tests writeAsField when propertyName is null
    @Test
    public void testWriteAsField_whenCannotWriteObjectIdAndPropertyNameNull_setsIdWrittenTrue() throws IOException {
        ObjectIdGenerators.IntSequenceGenerator generator = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(generator);
        woid.id = "val456";
        ObjectIdWriter writer = new ObjectIdWriter(stringType, null, generator, ToStringSerializer.instance, false);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        gen.writeStartObject();

        woid.writeAsField(gen, serializerProvider, writer);

        gen.writeEndObject();
        gen.flush();

        assertTrue(woid.idWritten);
        assertEquals("{}", sw.toString());
    }

    private static class NativeIdJsonGenerator extends JsonGeneratorDelegate {
        Object writtenRef;
        Object writtenId;

        NativeIdJsonGenerator(JsonGenerator delegate) {
            super(delegate);
        }

        @Override
        public boolean canWriteObjectId() {
            return true;
        }

        @Override
        public void writeObjectRef(Object id) throws IOException {
            this.writtenRef = id;
        }

        @Override
        public void writeObjectId(Object id) throws IOException {
            this.writtenId = id;
        }
    }
}