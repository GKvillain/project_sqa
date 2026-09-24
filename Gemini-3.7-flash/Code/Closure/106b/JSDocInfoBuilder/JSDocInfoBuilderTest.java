package com.google.javascript.rhino;

import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class JSDocInfoBuilderTest {

  private JSDocInfoBuilder builder;
  private JSDocInfoBuilder docBuilder;

  @Before
  public void setUp() {
    builder = new JSDocInfoBuilder(false);
    docBuilder = new JSDocInfoBuilder(true);
  }

  private JSTypeExpression createTypeExpression() {
    Node node = new Node(Token.NAME);
    return new JSTypeExpression(node, "test.js", (JSTypeRegistry) null);
  }

  // Tests build returns null when builder is not populated
  @Test
  public void testBuild_notPopulated_returnsNull() {
    assertFalse(builder.isPopulated());
    assertNull(builder.build("test.js"));
  }

  // Tests recording block description with parseDocumentation enabled and disabled
  @Test
  public void testRecordBlockDescription_parseDocumentationFlag() {
    assertTrue(builder.recordBlockDescription("A block description"));
    assertFalse(builder.isPopulated());

    assertTrue(docBuilder.recordBlockDescription("A block description"));
    assertTrue(docBuilder.isPopulated());
    JSDocInfo info = docBuilder.build("test.js");
    assertNotNull(info);
    assertEquals("A block description", info.getBlockDescription());
    assertEquals(Visibility.INHERITED, info.getVisibility());
  }

  // Tests recording visibility and duplicate visibility prevention
  @Test
  public void testRecordVisibility_validAndDuplicate() {
    assertTrue(builder.recordVisibility(Visibility.PRIVATE));
    assertTrue(builder.isPopulated());
    assertFalse(builder.recordVisibility(Visibility.PUBLIC));

    JSDocInfo info = builder.build("test.js");
    assertNotNull(info);
    assertEquals(Visibility.PRIVATE, info.getVisibility());
    assertEquals("test.js", info.getSourceName());
    assertFalse(builder.isPopulated());
  }

  // Tests recording parameter with and without singleton type conflict
  @Test
  public void testRecordParameter_normalAndConflictWithSingletonType() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordParameter("param1", typeExpr));
    assertTrue(builder.hasParameter("param1"));
    assertFalse(builder.recordParameter("param1", typeExpr));

    JSDocInfoBuilder builderWithType = new JSDocInfoBuilder(false);
    assertTrue(builderWithType.recordType(typeExpr));
    assertFalse(builderWithType.recordParameter("param2", typeExpr));
  }

  // Tests parameter description recording
  @Test
  public void testRecordParameterDescription_successAndDuplicate() {
    assertTrue(builder.recordParameterDescription("arg0", "the description"));
    assertTrue(builder.isPopulated());
    assertFalse(builder.recordParameterDescription("arg0", "duplicate"));
  }

  // Tests recording template type name
  @Test
  public void testRecordTemplateTypeName_successAndDuplicate() {
    assertTrue(builder.recordTemplateTypeName("T"));
    assertTrue(builder.isPopulated());
    assertFalse(builder.recordTemplateTypeName("T"));
  }

  // Tests recording throw type and throw description
  @Test
  public void testRecordThrowTypeAndDescription() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordThrowType(typeExpr));
    assertTrue(builder.isPopulated());
    assertTrue(builder.recordThrowDescription(typeExpr, "throws error"));
  }

  // Tests adding author, reference, version, deprecation, and suppressions
  @Test
  public void testAddAuthorReferenceVersionDeprecationSuppressions() {
    assertTrue(builder.addAuthor("Author Name"));
    assertTrue(builder.addReference("Reference Link"));
    assertTrue(builder.recordVersion("1.0.0"));
    assertTrue(builder.recordDeprecationReason("Deprecated reason"));
    
    Set<String> suppressions = new HashSet<String>();
    suppressions.add("checkTypes");
    assertTrue(builder.recordSuppressions(suppressions));
    assertTrue(builder.isPopulated());

    JSDocInfo info = builder.build("test.js");
    assertNotNull(info);
    assertTrue(info.isDeprecated());
    assertEquals("Deprecated reason", info.getDeprecationReason());
    assertEquals("1.0.0", info.getVersion());
  }

  // Tests type related tags incompatibility with recordType, recordTypedef, and recordEnumParameterType
  @Test
  public void testRecordTypeRelatedTags_incompatibility() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordConstructor());
    assertTrue(builder.isConstructorRecorded());
    assertFalse(builder.recordType(typeExpr));
    assertFalse(builder.recordTypedef(typeExpr));
    assertFalse(builder.recordEnumParameterType(typeExpr));
  }

  // Tests return type recording and duplicate rejection
  @Test
  public void testRecordReturnTypeAndDescription() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordReturnType(typeExpr));
    assertFalse(builder.recordReturnType(typeExpr));
    assertTrue(builder.recordReturnDescription("returns a value"));
  }

  // Tests define type recording and constancy interaction
  @Test
  public void testRecordDefineType_normalAndConstancy() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordDefineType(typeExpr));
    assertFalse(builder.recordDefineType(typeExpr));

    JSDocInfoBuilder builderConst = new JSDocInfoBuilder(false);
    assertTrue(builderConst.recordConstancy());
    assertFalse(builderConst.recordDefineType(typeExpr));
  }

  // Tests recording this type and base type
  @Test
  public void testRecordThisTypeAndBaseType() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordThisType(typeExpr));
    assertFalse(builder.recordThisType(typeExpr));
    assertTrue(builder.recordBaseType(typeExpr));
    assertFalse(builder.recordBaseType(typeExpr));
  }

  // Tests recording description and file overview
  @Test
  public void testRecordDescriptionAndFileOverview() {
    assertFalse(builder.isDescriptionRecorded());
    assertTrue(builder.recordDescription("i18n description"));
    assertTrue(builder.isDescriptionRecorded());
    assertFalse(builder.recordDescription("another description"));

    assertFalse(builder.isPopulatedWithFileOverview());
    assertTrue(builder.recordFileOverview("file overview text"));
    assertTrue(builder.isPopulatedWithFileOverview());
  }

  // Tests constructor and interface exclusivity
  @Test
  public void testConstructorAndInterfaceExclusivity() {
    assertTrue(builder.recordConstructor());
    assertTrue(builder.isConstructorRecorded());
    assertFalse(builder.recordConstructor());
    assertFalse(builder.recordInterface());
    assertFalse(builder.isInterfaceRecorded());

    JSDocInfoBuilder interfaceBuilder = new JSDocInfoBuilder(false);
    assertTrue(interfaceBuilder.recordInterface());
    assertTrue(interfaceBuilder.isInterfaceRecorded());
    assertFalse(interfaceBuilder.recordInterface());
    assertFalse(interfaceBuilder.recordConstructor());
  }

  // Tests boolean flags: hidden, noTypeCheck, preserveTry, override, noAlias, deprecated, export, noShadow, implicitCast, noSideEffects
  @Test
  public void testRecordBooleanFlags() {
    assertTrue(builder.recordHiddenness());
    assertFalse(builder.recordHiddenness());

    assertTrue(builder.recordNoTypeCheck());
    assertFalse(builder.recordNoTypeCheck());

    assertTrue(builder.recordPreserveTry());
    assertFalse(builder.recordPreserveTry());

    assertTrue(builder.recordOverride());
    assertFalse(builder.recordOverride());

    assertTrue(builder.recordNoAlias());
    assertFalse(builder.recordNoAlias());

    assertTrue(builder.recordDeprecated());
    assertFalse(builder.recordDeprecated());

    assertTrue(builder.recordExport());
    assertFalse(builder.recordExport());

    assertTrue(builder.recordNoShadow());
    assertFalse(builder.recordNoShadow());

    assertTrue(builder.recordImplicitCast());
    assertFalse(builder.recordImplicitCast());

    assertTrue(builder.recordNoSideEffects());
    assertFalse(builder.recordNoSideEffects());
  }

  // Tests recording implemented interface
  @Test
  public void testRecordImplementedInterface() {
    JSTypeExpression typeExpr = createTypeExpression();

    assertTrue(builder.recordImplementedInterface(typeExpr));
    assertTrue(builder.isPopulated());
  }

  // Tests markers recording with text, type, and name annotations
  @Test
  public void testMarkersRecording() {
    builder.markAnnotation("param", 10, 5);
    builder.markText("param doc", 10, 15, 10, 24);
    Node node = new Node(Token.NAME);
    builder.markTypeNode(node, 10, 11, 14, true);
    builder.markName("paramName", 10, 26);

    // Call markers without active currentMarker should not throw
    JSDocInfoBuilder emptyMarkerBuilder = new JSDocInfoBuilder(false);
    emptyMarkerBuilder.markText("text", 1, 1, 1, 5);
    emptyMarkerBuilder.markTypeNode(node, 1, 1, 5, false);
    emptyMarkerBuilder.markName("name", 1, 1);
  }

  // Tests shouldParseDocumentation and lends/meaning/extended interfaces
  @Test
  public void testAdditionalBuilderMethods() {
    assertFalse(builder.shouldParseDocumentation());
    assertTrue(docBuilder.shouldParseDocumentation());

    assertTrue(builder.recordLends("foo.bar"));
    assertFalse(builder.recordLends("foo.baz"));

    assertTrue(builder.recordMeaning("custom meaning"));
    assertFalse(builder.recordMeaning("duplicate meaning"));

    JSTypeExpression typeExpr = createTypeExpression();
    assertTrue(builder.recordExtendedInterface(typeExpr));
  }
}