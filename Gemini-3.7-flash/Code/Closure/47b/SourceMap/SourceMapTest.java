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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SourceMapTest {

  private SourceMap sourceMap;

  @Before
  public void setUp() {
    sourceMap = SourceMap.Format.V3.getInstance();
  }

  // Tests Format enum getInstance for all available formats
  @Test
  public void testFormat_getInstance_returnsNonNullInstances() {
    assertNotNull(SourceMap.Format.V1.getInstance());
    assertNotNull(SourceMap.Format.V2.getInstance());
    assertNotNull(SourceMap.Format.V3.getInstance());
    assertNotNull(SourceMap.Format.DEFAULT.getInstance());
  }

  // Tests DetailLevel.ALL returns true for any node
  @Test
  public void testDetailLevel_all_returnsTrueForAllNodes() {
    Node stringNode = Node.newString("foo");
    Node numberNode = Node.newNumber(42);
    assertTrue(SourceMap.DetailLevel.ALL.apply(stringNode));
    assertTrue(SourceMap.DetailLevel.ALL.apply(numberNode));
  }

  // Tests DetailLevel.SYMBOLS returns true for symbol-like nodes and false for others
  @Test
  public void testDetailLevel_symbols_filtersCorrectNodeTypes() {
    Node callNode = new Node(Token.CALL);
    Node newNode = new Node(Token.NEW);
    Node functionNode = new Node(Token.FUNCTION);
    Node nameNode = Node.newString(Token.NAME, "myVar");
    Node numberNode = Node.newNumber(123);

    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(callNode));
    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(newNode));
    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(functionNode));
    assertTrue(SourceMap.DetailLevel.SYMBOLS.apply(nameNode));
    assertFalse(SourceMap.DetailLevel.SYMBOLS.apply(numberNode));
  }

  // Tests addMapping with valid node produces expected mapping in output
  @Test
  public void testAddMapping_validNode_generatesMapping() throws IOException {
    Node node = Node.newString("test");
    node.putProp(Node.SOURCENAME_PROP, "source.js");
    node.setLineno(10);
    node.setCharno(5);

    sourceMap.addMapping(node, new FilePosition(1, 0), new FilePosition(1, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertTrue(result.contains("source.js"));
  }

  // Tests addMapping with null source file skips mapping
  @Test
  public void testAddMapping_nullSourceFile_ignoresMapping() throws IOException {
    Node node = Node.newString("test");
    node.putProp(Node.SOURCENAME_PROP, null);
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertFalse(result.contains("sources\":[\"null\"]"));
  }

  // Tests addMapping with negative line number skips mapping
  @Test
  public void testAddMapping_negativeLineno_ignoresMapping() throws IOException {
    Node node = Node.newString("test");
    node.putProp(Node.SOURCENAME_PROP, "source.js");
    node.setLineno(-1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 4));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertFalse(result.contains("source.js"));
  }

  // Tests addMapping when node has ORIGINALNAME_PROP
  @Test
  public void testAddMapping_withOriginalName_includesOriginalNameInMap() throws IOException {
    Node node = Node.newString(Token.NAME, "renamedVar");
    node.putProp(Node.SOURCENAME_PROP, "source.js");
    node.setLineno(1);
    node.setCharno(0);
    node.putProp(Node.ORIGINALNAME_PROP, "originalVar");

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 10));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertTrue(result.contains("originalVar"));
  }

  // Tests prefix replacement mapping on source file paths
  @Test
  public void testFixupSourceLocation_matchingPrefix_replacesPrefix() throws IOException {
    List<SourceMap.LocationMapping> mappings = Lists.newArrayList();
    mappings.add(new SourceMap.LocationMapping("http://server/js/", "src/"));
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString("foo");
    node.putProp(Node.SOURCENAME_PROP, "http://server/js/app.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertTrue(result.contains("src/app.js"));
    assertFalse(result.contains("http://server/js/app.js"));
  }

  // Tests caching mechanism when multiple nodes reference the same source file
  @Test
  public void testFixupSourceLocation_cacheHit_usesCachedValue() throws IOException {
    List<SourceMap.LocationMapping> mappings = Collections.singletonList(
        new SourceMap.LocationMapping("/prefix/", "/fixed/")
    );
    sourceMap.setPrefixMappings(mappings);

    Node node1 = Node.newString("foo");
    node1.putProp(Node.SOURCENAME_PROP, "/prefix/file.js");
    node1.setLineno(1);
    node1.setCharno(0);

    Node node2 = Node.newString("bar");
    node2.putProp(Node.SOURCENAME_PROP, "/prefix/file.js");
    node2.setLineno(2);
    node2.setCharno(0);

    sourceMap.addMapping(node1, new FilePosition(0, 0), new FilePosition(0, 3));
    sourceMap.addMapping(node2, new FilePosition(1, 0), new FilePosition(1, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertTrue(result.contains("/fixed/file.js"));
  }

  // Tests prefix replacement when no prefixes match
  @Test
  public void testFixupSourceLocation_noMatchingPrefix_retainsOriginalPath() throws IOException {
    List<SourceMap.LocationMapping> mappings = Collections.singletonList(
        new SourceMap.LocationMapping("/unmatched/", "/fixed/")
    );
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString("foo");
    node.putProp(Node.SOURCENAME_PROP, "/other/file.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertTrue(result.contains("/other/file.js"));
  }

  // Tests reset clears generator state and fixup cache
  @Test
  public void testReset_clearsStateAndCache() throws IOException {
    Node node = Node.newString("foo");
    node.putProp(Node.SOURCENAME_PROP, "source.js");
    node.setLineno(1);
    node.setCharno(0);

    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));
    sourceMap.reset();

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "output.js");
    String result = sb.toString();

    assertFalse(result.contains("source.js"));
  }

  // Tests setting starting line and index offset
  @Test
  public void testSetStartingPosition_executesWithoutError() {
    sourceMap.setStartingPosition(10, 5);
  }

  // Tests setting wrapper prefix
  @Test
  public void testSetWrapperPrefix_executesWithoutError() {
    sourceMap.setWrapperPrefix("prefix;");
  }

  // Tests validate method flag
  @Test
  public void testValidate_executesWithoutError() {
    sourceMap.validate(true);
    sourceMap.validate(false);
  }
}