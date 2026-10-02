# Zest – JxPath-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 339511 (valid 90.12%) |
| Cycles | 22 |
| Corpus | 43 |
| Zest branch coverage (total / valid) | 203 / 200 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 43 |
| Failing on fixed | 0 |
| Failing on buggy | 8 |
| Bug detection | Fail (triggering: 8) |
| **Fault detected** | **yes** |
| Line coverage | 41 / 49 = 83.67% |
| Branch coverage | 29 / 40 = 72.50% |
| Test suite length (statements) | 43 |
| Mutation score | 38 / 48 = 79.17% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test24
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test33
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test35
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test36
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test39
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test40
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test41
- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test9

```
--- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test24
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan:!toString:java.lang.NullPointerException
1=java.lang.Boolean:false |this=org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan:!toString:java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test.test24(CoreOperationRelationalExpression_Zest_Test.java:175)
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
--- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test33
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:true |this=org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThan:!toString:java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test.test33(CoreOperationRelationalExpression_Zest_Test.java:238)
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
--- org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression_Zest_Test::test35
```

- Suite: `/home/user/suites/JxPath/zest/20/t12001/JxPath-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_20/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreOperationRelationalExpression_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_20/b120_r1`
