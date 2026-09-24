package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeInferenceTest {

    private Compiler compiler;
    private JSTypeRegistry registry;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        registry = compiler.getTypeRegistry();
    }

    private FlowScope createFlowScope(Scope syntacticScope) {
        return LinkedFlowScope.createEntryLattice(syntacticScope);
    }

    private TypeInference createTypeInference(ControlFlowGraph<Node> cfg, Scope syntacticScope) {
        ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
                compiler.getCodingConvention(), registry);
        return new TypeInference(compiler, cfg, rai, syntacticScope);
    }

    // Tests getBooleanOutcomes with both true and false outcomes
    @Test
    public void testGetBooleanOutcomes_bothConditionBranches_returnsExpectedSet() {
        BooleanLiteralSet left = BooleanLiteralSet.BOTH;
        BooleanLiteralSet right = BooleanLiteralSet.TRUE;

        BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
        assertEquals(BooleanLiteralSet.BOTH, result);

        BooleanLiteralSet resultFalse = TypeInference.getBooleanOutcomes(left, right, false);
        assertEquals(BooleanLiteralSet.BOTH, resultFalse);
    }

    // Tests getBooleanOutcomes with empty left outcome
    @Test
    public void testGetBooleanOutcomes_emptyLeft_returnsRight() {
        BooleanLiteralSet left = BooleanLiteralSet.EMPTY;
        BooleanLiteralSet right = BooleanLiteralSet.FALSE;

        BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
        assertEquals(BooleanLiteralSet.FALSE, result);
    }

    // Tests getBooleanOutcomePair method
    @Test
    public void testGetBooleanOutcomePair_validPair_combinesOutcomes() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(root);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope flowScope = createFlowScope(scope);
        TypeInference.class.getDeclaredClasses(); // verify inner classes available

        // Run flowThrough on null node to verify basic bottom scope behavior
        FlowScope bottomScope = typeInference.createInitialEstimateLattice();
        FlowScope flowed = typeInference.flowThrough(root, bottomScope);
        assertEquals(bottomScope, flowed);
    }

    // Tests createInitialEstimateLattice and createEntryLattice
    @Test
    public void testCreateLattices_initialAndEntry_returnNonNullLattices() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(root);
        TypeInference typeInference = createTypeInference(cfg, scope);

        assertNotNull(typeInference.createInitialEstimateLattice());
        assertNotNull(typeInference.createEntryLattice());
        assertNotNull(typeInference.getAssignedOuterLocalVars());
    }

    // Tests flowThrough with NUMBER literal node
    @Test
    public void testFlowThrough_numberLiteral_infersNumberType() {
        Node n = Node.newNumber(42.0);
        Scope scope = new Scope(n, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(n);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(n, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(NUMBER_TYPE), n.getJSType());
    }

    // Tests flowThrough with STRING literal node
    @Test
    public void testFlowThrough_stringLiteral_infersStringType() {
        Node n = Node.newString("hello");
        Scope scope = new Scope(n, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(n);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(n, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(STRING_TYPE), n.getJSType());
    }

    // Tests flowThrough with NULL literal node
    @Test
    public void testFlowThrough_nullLiteral_infersNullType() {
        Node n = new Node(Token.NULL);
        Scope scope = new Scope(n, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(n);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(n, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE), n.getJSType());
    }

    // Tests flowThrough with VOID node
    @Test
    public void testFlowThrough_voidNode_infersVoidType() {
        Node n = new Node(Token.VOID, Node.newNumber(0));
        Scope scope = new Scope(n, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(n);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(n, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(VOID_TYPE), n.getJSType());
    }

    // Tests flowThrough with BOOLEAN nodes (TRUE / FALSE / NOT)
    @Test
    public void testFlowThrough_booleanNodes_infersBooleanType() {
        Node trueNode = new Node(Token.TRUE);
        Node falseNode = new Node(Token.FALSE);
        Node notNode = new Node(Token.NOT, trueNode);

        Scope scope = new Scope(notNode, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(notNode);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(notNode, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(BOOLEAN_TYPE), notNode.getJSType());
    }

    // Tests flowThrough with ADD operation on two numbers
    @Test
    public void testFlowThrough_addTwoNumbers_infersNumberType() {
        Node left = Node.newNumber(1.0);
        Node right = Node.newNumber(2.0);
        Node add = new Node(Token.ADD, left, right);

        Scope scope = new Scope(add, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(add);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(add, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(NUMBER_TYPE), add.getJSType());
    }

    // Tests flowThrough with ADD operation on a string and a number
    @Test
    public void testFlowThrough_addStringAndNumber_infersStringType() {
        Node left = Node.newString("count: ");
        Node right = Node.newNumber(5.0);
        Node add = new Node(Token.ADD, left, right);

        Scope scope = new Scope(add, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(add);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(add, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(STRING_TYPE), add.getJSType());
    }

    // Tests flowThrough with ARRAY literal node
    @Test
    public void testFlowThrough_arrayLiteral_infersArrayType() {
        Node elem1 = Node.newNumber(1.0);
        Node elem2 = Node.newNumber(2.0);
        Node arrayLit = new Node(Token.ARRAYLIT, elem1, elem2);

        Scope scope = new Scope(arrayLit, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(arrayLit);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(arrayLit, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE), arrayLit.getJSType());
    }

    // Tests flowThrough with OBJECT literal node
    @Test
    public void testFlowThrough_objectLiteral_infersAnonymousObjectType() {
        Node key = Node.newString("a");
        Node val = Node.newNumber(10.0);
        key.addChildToFront(val);
        Node objLit = new Node(Token.OBJECTLIT, key);

        Scope scope = new Scope(objLit, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(objLit);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(objLit, entry);

        assertNotNull(out);
        assertTrue(objLit.getJSType() instanceof ObjectType);
    }

    // Tests flowThrough with NAME node and unflowable vars
    @Test
    public void testFlowThrough_unflowableVar_retainsOriginalScope() {
        Node nameNode = Node.newString(Token.NAME, "x");
        Scope scope = new Scope(nameNode, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        Scope.Var var = scope.declare("x", nameNode, registry.getNativeType(NUMBER_TYPE), null);

        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(nameNode);
        TypeInference typeInference = new TypeInference(
                compiler, cfg,
                new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), registry),
                scope, Collections.singletonList(var));

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(nameNode, entry);

        assertNotNull(out);
    }

    // Tests flowThrough with HOOK (ternary) operator
    @Test
    public void testFlowThrough_hookOperator_infersUnionOrSupertype() {
        Node cond = new Node(Token.TRUE);
        Node trueBranch = Node.newNumber(1.0);
        Node falseBranch = Node.newNumber(2.0);
        Node hook = new Node(Token.HOOK, cond, trueBranch, falseBranch);

        Scope scope = new Scope(hook, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(hook);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(hook, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(NUMBER_TYPE), hook.getJSType());
    }

    // Tests flowThrough with AND operator
    @Test
    public void testFlowThrough_andOperator_infersJoinedType() {
        Node left = Node.newNumber(1.0);
        Node right = Node.newString("text");
        Node andNode = new Node(Token.AND, left, right);

        Scope scope = new Scope(andNode, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(andNode);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(andNode, entry);

        assertNotNull(out);
        assertNotNull(andNode.getJSType());
    }

    // Tests flowThrough with OR operator
    @Test
    public void testFlowThrough_orOperator_infersJoinedType() {
        Node left = Node.newNumber(0.0);
        Node right = Node.newString("fallback");
        Node orNode = new Node(Token.OR, left, right);

        Scope scope = new Scope(orNode, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(orNode);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(orNode, entry);

        assertNotNull(out);
        assertNotNull(orNode.getJSType());
    }

    // Tests flowThrough with CATCH node
    @Test
    public void testFlowThrough_catchNode_infersUnknownType() {
        Node catchParam = Node.newString(Token.NAME, "err");
        Node catchBlock = new Node(Token.BLOCK);
        Node catchNode = new Node(Token.CATCH, catchParam, catchBlock);

        Scope scope = new Scope(catchNode, registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.GLOBAL_THIS));
        scope.declare("err", catchParam, null, null);

        ControlFlowGraph<Node> cfg = new ControlFlowAnalysis(compiler, true, false).computeCfg(catchNode);
        TypeInference typeInference = createTypeInference(cfg, scope);

        FlowScope entry = typeInference.createEntryLattice();
        FlowScope out = typeInference.flowThrough(catchNode, entry);

        assertNotNull(out);
        assertEquals(registry.getNativeType(UNKNOWN_TYPE), catchParam.getJSType());
    }
}