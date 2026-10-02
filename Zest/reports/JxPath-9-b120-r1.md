# Zest – JxPath-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 264586 (valid 90.44%) |
| Cycles | 25 |
| Corpus | 31 |
| Zest branch coverage (total / valid) | 183 / 180 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 31 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 37 / 68 = 54.41% |
| Branch coverage | 21 / 64 = 32.81% |
| Test suite length (statements) | 31 |
| Mutation score | 53 / 76 = 69.74% |
| Fuzz time | 120s |
| Pipeline time | 198s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test::test13
- org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test::test22
- org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test::test4

```
--- org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<...PointerException
1=![java.lang.NullPointer]Exception
> but was:<...PointerException
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test.test13(CoreOperationCompare_Zest_Test.java:98)
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
--- org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test::test22
junit.framework.AssertionFailedError: expected:<...PointerException
1=![java.lang.NullPointer]Exception
> but was:<...PointerException
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.compiler.CoreOperationCompare_Zest_Test.test22(CoreOperationCompare_Zest_Test.java:161)
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
```

- Suite: `/home/user/suites/JxPath/zest/9/t12001/JxPath-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_9/b120_r1/src/org/apache/commons/jxpath/ri/compiler/CoreOperationCompare_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_9/b120_r1`
