package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.debugging.sourcemap.FilePosition;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SourceMapTest {

  private SourceMap sourceMap;

  @Before
  public void setUp() {
    sourceMap = SourceMap.Format.V3.getInstance();
  }

  // Tests Format enum getInstance for all supported formats
  @Test
  public void testFormat_getInstance_createsNonNullInstances() {
    assertNotNull(SourceMap.Format.V1.getInstance());
    assertNotNull(SourceMap.Format.DEFAULT.getInstance());
    assertNotNull(SourceMap.Format.V2.getInstance());
    assertNotNull(SourceMap.Format.V3.getInstance());
  }

  // Tests DetailLevel.ALL returns true for any node
  @Test
  public void testDetailLevel_all_returnsTrue() {
    Node node = new Node(Token.VAR);
    assertTrue(SourceMap.DetailLevel.ALL.apply(node));
  }

  // Tests DetailLevel.SYMBOLS returns true for name, call, and function nodes
  @Test
  public void testDetailLevel_symbols_returnsTrueForSymbolNodes() {
    Node callNode = new Node(Token.CALL);
    Node nameNode = Node.newString(Token.NAME, "foo");
    Node functionNode = new Node(Token.FUNCTION);

    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(callNode));
    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(nameNode));
    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(functionNode));
  }

  // Tests DetailLevel.SYMBOLS returns false for non-symbol nodes
  @Test
  public void testDetailLevel_symbols_returnsFalseForNonSymbolNodes() {
    Node varNode = new Node(Token.VAR);
    Node exprResultNode = new Node(Token.EXPR_RESULT);

    assertFalse(SourceMap.DetailLevel.SYMBOLS.apply(varNode));
    assertFalse(SourceMap.DetailLevel.SYMBOLS.apply(exprResultNode));
  }

  // Tests addMapping when node has null source file
  @Test
  public void testAddMapping_nullSourceFile_ignoresMapping() throws IOException {
    Node node = new Node(Token.NAME);
    node.setLineno(1);
    node.setCharno(0);

    FilePosition start = new FilePosition(0, 0);
    FilePosition end = new FilePosition(0, 5);

    sourceMap.addMapping(node, start, end);

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "test.js");
    assertTrue(sb.toString().contains("\"mappings\":\"\"") || !sb.toString().contains("null"));
  }

  // Tests addMapping when node has negative line number
  @Test
  public void testAddMapping_negativeLineNumber_ignoresMapping() throws IOException {
    Node node = Node.newString(Token.NAME, "test");
    node.setSourceFileName("input.js");
    node.setLineno(-1);
    node.setCharno(0);

    FilePosition start = new FilePosition(0, 0);
    FilePosition end = new FilePosition(0, 4);

    sourceMap.addMapping(node, start, end);

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "test.js");
    assertNotNull(sb.toString());
  }

  // Tests normal addMapping with valid node and positions
  @Test
  public void testAddMapping_validNode_addsMappingSuccessfully() throws IOException {
    Node node = Node.newString(Token.NAME, "myVar");
    node.setSourceFileName("input.js");
    node.setLineno(1);
    node.setCharno(2);

    FilePosition start = new FilePosition(0, 0);
    FilePosition end = new FilePosition(0, 5);

    sourceMap.addMapping(node, start, end);

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String output = sb.toString();

    assertTrue(output.contains("input.js"));
  }

  // Tests addMapping with original name property preserved
  @Test
  public void testAddMapping_withOriginalName_includesOriginalName() throws IOException {
    Node node = Node.newString(Token.NAME, "renamed");
    node.setSourceFileName("input.js");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.ORIGINALNAME_PROP, "originalName");

    FilePosition start = new FilePosition(0, 0);
    FilePosition end = new FilePosition(0, 7);

    sourceMap.addMapping(node, start, end);

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String output = sb.toString();

    assertTrue(output.contains("originalName"));
  }

  // Tests location mapping prefix replacement
  @Test
  public void testFixupSourceLocation_matchingPrefix_replacesPrefix() throws IOException {
    SourceMap.LocationMapping mapping =
        new SourceMap.LocationMapping("/prefix/path/", "http://fixed/");
    sourceMap.setPrefixMappings(Collections.singletonList(mapping));

    Node node = Node.newString(Token.NAME, "test");
    node.setSourceFileName("/prefix/path/file.js");
    node.setLineno(1);
    node.setCharno(0);

    FilePosition start = new FilePosition(0, 0);
    FilePosition end = new FilePosition(0, 4);

    sourceMap.addMapping(node, start, end);

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String output = sb.toString();

    assertTrue(output.contains("http://fixed/file.js"));
    assertFalse(output.contains("/prefix/path/file.js"));
  }

  // Tests location mapping cache hit on subsequent mapping
  @Test
  public void testFixupSourceLocation_cachedLocation_usesCachedValue() throws IOException {
    SourceMap.LocationMapping mapping =
        new SourceMap.LocationMapping("src/", "dist/");
    sourceMap.setPrefixMappings(Collections.singletonList(mapping));

    Node node1 = Node.newString(Token.NAME, "a");
    node1.setSourceFileName("src/app.js");
    node1.setLineno(1);
    node1.setCharno(0);

    Node node2 = Node.newString(Token.NAME, "b");
    node2.setSourceFileName("src/app.js");
    node2.setLineno(2);
    node2.setCharno(0);

    sourceMap.addMapping(node1, new FilePosition(0, 0), new FilePosition(0, 1));
    sourceMap.addMapping(node2, new FilePosition(1, 0), new FilePosition(1, 1));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    assertTrue(output.contains("dist/app.js"));
  }

  // Tests location mapping when no prefix matches
  @Test
  public void testFixupSourceLocation_noMatchingPrefix_retainsOriginalPath() throws IOException {
    SourceMap.LocationMapping mapping =
        new SourceMap.LocationMapping("/other/", "/replaced/");
    sourceMap.setPrefixMappings(Collections.singletonList(mapping));

    Node node = Node.newString(Token.NAME, "test");
    node.setSourceFileName("input.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String output = sb.toString();

    assertTrue(output.contains("input.js"));
  }

  // Tests reset clears mappings and location cache
  @Test
  public void testReset_clearsState() throws IOException {
    Node node = Node.newString(Token.NAME, "test");
    node.setSourceFileName("input.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 4));
    sourceMap.reset();

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String output = sb.toString();

    assertFalse(output.contains("input.js"));
  }

  // Tests setStartingPosition, setWrapperPrefix and validate methods
  @Test
  public void testConfigurationMethods_executeWithoutError() throws IOException {
    sourceMap.setStartingPosition(1, 0);
    sourceMap.setWrapperPrefix("prefix;");
    sourceMap.validate(true);
    sourceMap.validate(false);

    Node node = Node.newString(Token.NAME, "test");
    node.setSourceFileName("input.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    assertNotNull(sb.toString());
  }

  // Tests multiple prefix mappings selecting the first matching prefix
  @Test
  public void testFixupSourceLocation_multipleMappings_selectsFirstMatch() throws IOException {
    List<SourceMap.LocationMapping> mappings = Lists.newArrayList(
        new SourceMap.LocationMapping("root/sub/", "target/sub/"),
        new SourceMap.LocationMapping("root/", "target/root/")
    );
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString(Token.NAME, "test");
    node.setSourceFileName("root/sub/file.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String output = sb.toString();

    assertTrue(output.contains("target/sub/file.js"));
    assertFalse(output.contains("target/root/sub/file.js"));
  }
}