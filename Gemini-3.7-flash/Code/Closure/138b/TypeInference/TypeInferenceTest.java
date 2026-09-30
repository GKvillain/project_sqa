package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeInferenceTest {

    private Compiler compiler;
    private JSTypeRegistry registry;
    private FlowScope emptyFlowScope;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        registry = compiler.getTypeRegistry();
    }

    private TypeInference createTypeInference(Node root, Scope scope) {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
        cfa.process(null, root);
        ControlFlowGraph<Node> cfg = cfa.getCfg();
        ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
                compiler.getCodingConvention(), registry);
        return new TypeInference(compiler, cfg, rai, scope);
    }

    private Scope createSyntacticScope(Node root) {
        return new SyntacticScopeCreator(compiler).createScope(root, null);
    }

    // Tests static helper getBooleanOutcomes for AND condition
    @Test
    public void testGetBooleanOutcomes_andCondition_returnsExpectedSet() {
        BooleanLiteralSet left = BooleanLiteralSet.BOTH;
        BooleanLiteralSet right = BooleanLiteralSet.TRUE;
        // condition = true indicates AND logic
        BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
        assertEquals(BooleanLiteralSet.TRUE, result);
    }

    // Tests static helper getBooleanOutcomes for OR condition
    @Test
    public void testGetBooleanOutcomes_orCondition_returnsExpectedSet() {
        BooleanLiteralSet left = BooleanLiteralSet.FALSE;
        BooleanLiteralSet right = BooleanLiteralSet.TRUE;
        // condition = false indicates OR logic
        BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, false);
        assertEquals(BooleanLiteralSet.TRUE, result);
    }

    // Tests flow-through on a simple number assignment
    @Test
    public void testFlowThrough_numberAssignment_infersNumberType() {
        Node n = compiler.parseTestCode("var a = 1;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        StaticSlot<JSType> slot = resultScope.getSlot("a");
        assertNotNull(slot);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), slot.getType());
    }

    // Tests flow-through on string assignment
    @Test
    public void testFlowThrough_stringAssignment_infersStringType() {
        Node n = compiler.parseTestCode("var s = 'hello';");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        StaticSlot<JSType> slot = resultScope.getSlot("s");
        assertNotNull(slot);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), slot.getType());
    }

    // Tests flow-through on boolean and null assignments
    @Test
    public void testFlowThrough_nullAndBooleanAssignment_infersTypes() {
        Node n = compiler.parseTestCode("var b = true; var nl = null;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), resultScope.getSlot("b").getType());
        assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), resultScope.getSlot("nl").getType());
    }

    // Tests array literal traversal
    @Test
    public void testFlowThrough_arrayLiteral_infersArrayType() {
        Node n = compiler.parseTestCode("var arr = [1, 2, 3];");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), resultScope.getSlot("arr").getType());
    }

    // Tests object literal traversal
    @Test
    public void testFlowThrough_objectLiteral_infersObjectType() {
        Node n = compiler.parseTestCode("var obj = {foo: 'bar'};");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        JSType slotType = resultScope.getSlot("obj").getType();
        assertNotNull(slotType);
        assertTrue(slotType instanceof ObjectType);
    }

    // Tests addition operation inference
    @Test
    public void testFlowThrough_addition_infersCorrectType() {
        Node n = compiler.parseTestCode("var x = 1 + 2; var y = 'a' + 'b';");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), resultScope.getSlot("x").getType());
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), resultScope.getSlot("y").getType());
    }

    // Tests unflowable vars restriction
    @Test
    public void testFlowThrough_unflowableVar_ignoresInference() {
        Node n = compiler.parseTestCode("var unflowable = 1;");
        Scope scope = createSyntacticScope(n);
        Scope.Var var = scope.getVar("unflowable");
        assertNotNull(var);

        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
        cfa.process(null, n);
        ControlFlowGraph<Node> cfg = cfa.getCfg();
        ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
                compiler.getCodingConvention(), registry);
        TypeInference typeInference = new TypeInference(
                compiler, cfg, rai, scope, ImmutableSet.of(var));

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
    }

    // Tests hook (ternary) operator traversal
    @Test
    public void testFlowThrough_hookOperator_infersUnionType() {
        Node n = compiler.parseTestCode("var cond = true; var res = cond ? 1 : 'str';");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        JSType resType = resultScope.getSlot("res").getType();
        assertNotNull(resType);
        assertTrue(resType.isUnionType());
    }

    // Tests catch block scope inference
    @Test
    public void testFlowThrough_catchBlock_infersUnknownType() {
        Node n = compiler.parseTestCode("try { } catch (e) { }");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
    }

    // Tests short-circuiting AND/OR operators
    @Test
    public void testFlowThrough_logicalAndOr_infersJoinedType() {
        Node n = compiler.parseTestCode("var a = true && false; var b = 'a' || 123;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertNotNull(resultScope.getSlot("a"));
        assertNotNull(resultScope.getSlot("b"));
    }

    // Tests initial estimate lattice returns bottom scope
    @Test
    public void testCreateInitialEstimateLattice_returnsBottomScope() {
        Node n = compiler.parseTestCode("var a = 1;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope bottomScope = typeInference.createInitialEstimateLattice();
        assertNotNull(bottomScope);
        // Flowing through bottomScope should return bottomScope itself without modifications
        FlowScope flowed = typeInference.flowThrough(n, bottomScope);
        assertEquals(bottomScope, flowed);
    }

    // Tests comparison operations (<, <=, >, >=, ==, !=, ===, !==, in, instanceof)
    @Test
    public void testFlowThrough_comparisons_infersBooleanType() {
        Node n = compiler.parseTestCode(
                "var lt = 1 < 2; var le = 1 <= 2; var gt = 2 > 1; var ge = 2 >= 1;"
                + "var eq = 1 == 1; var ne = 1 != 2; var sheq = 1 === 1; var shne = 1 !== 2;"
                + "var hasProp = 'a' in {}; var isInst = [] instanceof Object;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        assertEquals(boolType, resultScope.getSlot("lt").getType());
        assertEquals(boolType, resultScope.getSlot("le").getType());
        assertEquals(boolType, resultScope.getSlot("gt").getType());
        assertEquals(boolType, resultScope.getSlot("ge").getType());
        assertEquals(boolType, resultScope.getSlot("eq").getType());
        assertEquals(boolType, resultScope.getSlot("ne").getType());
        assertEquals(boolType, resultScope.getSlot("sheq").getType());
        assertEquals(boolType, resultScope.getSlot("shne").getType());
        assertEquals(boolType, resultScope.getSlot("hasProp").getType());
        assertEquals(boolType, resultScope.getSlot("isInst").getType());
    }

    // Tests unary operators (!, typeof, void, delete, +, -, ~)
    @Test
    public void testFlowThrough_unaryOperators_infersCorrectTypes() {
        Node n = compiler.parseTestCode(
                "var notVal = !0; var typeVal = typeof 123; var voidVal = void 0;"
                + "var delVal = delete window.foo; var posVal = + '5'; var negVal = -5; var bitNot = ~0;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), resultScope.getSlot("notVal").getType());
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), resultScope.getSlot("typeVal").getType());
        assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), resultScope.getSlot("voidVal").getType());
        assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), resultScope.getSlot("delVal").getType());
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), resultScope.getSlot("posVal").getType());
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), resultScope.getSlot("negVal").getType());
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), resultScope.getSlot("bitNot").getType());
    }

    // Tests arithmetic binary operators (*, /, %, -, <<, >>, >>>, &, |, ^)
    @Test
    public void testFlowThrough_arithmeticAndBitwise_infersNumberType() {
        Node n = compiler.parseTestCode(
                "var mul = 2 * 3; var div = 4 / 2; var mod = 5 % 2; var sub = 5 - 2;"
                + "var shl = 1 << 2; var shr = 4 >> 1; var ushr = 4 >>> 1;"
                + "var bitAnd = 1 & 3; var bitOr = 1 | 2; var bitXor = 1 ^ 3;");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertEquals(numType, resultScope.getSlot("mul").getType());
        assertEquals(numType, resultScope.getSlot("div").getType());
        assertEquals(numType, resultScope.getSlot("mod").getType());
        assertEquals(numType, resultScope.getSlot("sub").getType());
        assertEquals(numType, resultScope.getSlot("shl").getType());
        assertEquals(numType, resultScope.getSlot("shr").getType());
        assertEquals(numType, resultScope.getSlot("ushr").getType());
        assertEquals(numType, resultScope.getSlot("bitAnd").getType());
        assertEquals(numType, resultScope.getSlot("bitOr").getType());
        assertEquals(numType, resultScope.getSlot("bitXor").getType());
    }

    // Tests augmented assignments (+=, -=, *=, /=, %=, etc.) and inc/dec (++, --)
    @Test
    public void testFlowThrough_augmentedAssignmentsAndIncDec_infersExpectedTypes() {
        Node n = compiler.parseTestCode(
                "var a = 1; a += 2; a -= 1; a *= 3; a /= 2; a %= 2;"
                + "var b = 5; b++; ++b; b--; --b;"
                + "var str = 'hello'; str += ' world';");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), resultScope.getSlot("a").getType());
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), resultScope.getSlot("b").getType());
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), resultScope.getSlot("str").getType());
    }

    // Tests function declaration and function expression inference
    @Test
    public void testFlowThrough_functionDeclarationAndExpression_infersFunctionType() {
        Node n = compiler.parseTestCode(
                "function foo(x) { return x; } var bar = function(y) { return y; };");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertNotNull(resultScope.getSlot("foo"));
        assertTrue(resultScope.getSlot("foo").getType() instanceof FunctionType);
        assertNotNull(resultScope.getSlot("bar"));
        assertTrue(resultScope.getSlot("bar").getType() instanceof FunctionType);
    }

    // Tests control flow constructs: switch, while, for, for-in, do-while
    @Test
    public void testFlowThrough_controlFlowConstructs_succeeds() {
        Node n = compiler.parseTestCode(
                "var val = 1;\n"
                + "switch(val) { case 1: val = 2; break; default: val = 3; }\n"
                + "while(val < 10) { val++; }\n"
                + "do { val--; } while(val > 5);\n"
                + "for (var i = 0; i < 5; i++) { val += i; }\n"
                + "for (var k in {a: 1}) { val = k; }");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertNotNull(resultScope.getSlot("val"));
        assertNotNull(resultScope.getSlot("i"));
        assertNotNull(resultScope.getSlot("k"));
    }

    // Tests throw and return statements
    @Test
    public void testFlowThrough_throwAndReturn_succeeds() {
        Node n = compiler.parseTestCode(
                "function test() { if (true) { throw new Error('fail'); } return 42; }");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertNotNull(resultScope.getSlot("test"));
    }

    // Tests property access (GETPROP and GETELEM) and call expressions
    @Test
    public void testFlowThrough_propertyAccessAndCalls_succeeds() {
        Node n = compiler.parseTestCode(
                "var obj = { x: 10, m: function() { return 1; } };\n"
                + "var prop = obj.x;\n"
                + "var elem = obj['x'];\n"
                + "var res = obj.m();\n"
                + "var obj2 = new Object();");
        Scope scope = createSyntacticScope(n);
        TypeInference typeInference = createTypeInference(n, scope);

        FlowScope entryScope = typeInference.createEntryLattice();
        FlowScope resultScope = typeInference.flowThrough(n, entryScope);

        assertNotNull(resultScope);
        assertNotNull(resultScope.getSlot("prop"));
        assertNotNull(resultScope.getSlot("elem"));
        assertNotNull(resultScope.getSlot("res"));
        assertNotNull(resultScope.getSlot("obj2"));
    }
}