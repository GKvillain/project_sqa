# Zest – JxPath-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 358960 (valid 89.94%) |
| Cycles | 27 |
| Corpus | 39 |
| Zest branch coverage (total / valid) | 197 / 194 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 39 |
| Failing on fixed | 0 |
| Failing on buggy | 15 |
| Bug detection | Fail (triggering: 15) |
| **Fault detected** | **yes** |
| Line coverage | 34 / 43 = 79.07% |
| Branch coverage | 24 / 36 = 66.67% |
| Test suite length (statements) | 39 |
| Mutation score | 36 / 46 = 78.26% |
| Fuzz time | 120s |
| Pipeline time | 187s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test11
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test15
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test17
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test19
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test20
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test23
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test25
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test26
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test28
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test35
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test38
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test3
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test4
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test7
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test9

```
--- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[fals]e |this=org.apache.c...> but was:<0=java.lang.Boolean:[tru]e |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test.test11(CoreOperationRelationalExpression_Zest_Test.java:84)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:292)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:829)
--- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[fals]e |this=org.apache.c...> but was:<0=java.lang.Boolean:[tru]e |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test.test15(CoreOperationRelationalExpression_Zest_Test.java:112)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:292)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:829)
--- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[fals]e |this=org.apache.c...> but was:<0=java.lang.Boolean:[tru]e |this=org.apache.c...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test.test17(CoreOperationRelationalExpression_Zest_Test.java:126)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
```

- Suite: `/home/user/suites/JxPath/zest/8/t12001/JxPath-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_8/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreOperationRelationalExpression_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_8/b120_r1`
