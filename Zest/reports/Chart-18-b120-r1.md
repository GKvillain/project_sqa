# Zest – Chart-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 93473 (valid 79.67%) |
| Cycles | 6 |
| Corpus | 52 |
| Zest branch coverage (total / valid) | 201 / 198 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 52 |
| Failing on fixed | 0 |
| Failing on buggy | 14 |
| Bug detection | Fail (triggering: 14) |
| **Fault detected** | **yes** |
| Line coverage | 71 / 271 = 26.20% |
| Branch coverage | 19 / 126 = 15.08% |
| Test suite length (statements) | 52 |
| Mutation score | 291 / 318 = 91.51% |
| Fuzz time | 120s |
| Pipeline time | 195s |

## Failing tests on buggy version

- org.jfree.data.DefaultKeyedValues_Zest_Test::test13
- org.jfree.data.DefaultKeyedValues_Zest_Test::test16
- org.jfree.data.DefaultKeyedValues_Zest_Test::test20
- org.jfree.data.DefaultKeyedValues_Zest_Test::test24
- org.jfree.data.DefaultKeyedValues_Zest_Test::test27
- org.jfree.data.DefaultKeyedValues_Zest_Test::test28
- org.jfree.data.DefaultKeyedValues_Zest_Test::test31
- org.jfree.data.DefaultKeyedValues_Zest_Test::test43
- org.jfree.data.DefaultKeyedValues_Zest_Test::test50
- org.jfree.data.DefaultKeyedValues_Zest_Test::test0
- org.jfree.data.DefaultKeyedValues_Zest_Test::test1
- org.jfree.data.DefaultKeyedValues_Zest_Test::test2
- org.jfree.data.DefaultKeyedValues_Zest_Test::test5
- org.jfree.data.DefaultKeyedValues_Zest_Test::test6

```
--- org.jfree.data.DefaultKeyedValues_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<...aultKeyedValues@1
1=[!org.jfree.data.UnknownKeyException]
> but was:<...aultKeyedValues@1
1=[void |this=org.jfree.data.DefaultKeyedValues:org.jfree.data.DefaultKeyedValues@1]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.DefaultKeyedValues_Zest_Test.test13(DefaultKeyedValues_Zest_Test.java:98)
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
--- org.jfree.data.DefaultKeyedValues_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[!org.jfree.data.UnknownKeyException]
> but was:<0=[void |this=org.jfree.data.DefaultKeyedValues2D:org.jfree.data.DefaultKeyedValues2D@367
1=void |this=org.jfree.data.DefaultKeyedValues:org.jfree.data.DefaultKeyedValues@1]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.DefaultKeyedValues_Zest_Test.test16(DefaultKeyedValues_Zest_Test.java:119)
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
```

- Suite: `/home/pilaiphon/suites/Chart/zest/18/t12001/Chart-18f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_18/b120_r1/src/org/jfree/data/DefaultKeyedValues_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_18/b120_r1`
