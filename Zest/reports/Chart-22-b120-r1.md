# Zest – Chart-22 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 24515 (valid 65.40%) |
| Cycles | 1 |
| Corpus | 54 |
| Zest branch coverage (total / valid) | 206 / 203 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 54 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 83 / 145 = 57.24% |
| Branch coverage | 28 / 74 = 37.84% |
| Test suite length (statements) | 54 |
| Mutation score | 123 / 174 = 70.69% |
| Fuzz time | 121s |
| Pipeline time | 275s |

## Failing tests on buggy version

- org.jfree.data.KeyedObjects2D_Zest_Test::test17
- org.jfree.data.KeyedObjects2D_Zest_Test::test31
- org.jfree.data.KeyedObjects2D_Zest_Test::test41
- org.jfree.data.KeyedObjects2D_Zest_Test::test45
- org.jfree.data.KeyedObjects2D_Zest_Test::test51
- org.jfree.data.KeyedObjects2D_Zest_Test::test3

```
--- org.jfree.data.KeyedObjects2D_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=![org.jfree.data.UnknownKey]Exception
> but was:<0=![java.lang.IndexOutOfBounds]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.KeyedObjects2D_Zest_Test.test17(KeyedObjects2D_Zest_Test.java:126)
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
--- org.jfree.data.KeyedObjects2D_Zest_Test::test31
junit.framework.AssertionFailedError: expected:<...data.KeyedObjects2D:[org.jfree.data.KeyedObjects2D@367]
1=!java.lang.IndexO...> but was:<...data.KeyedObjects2D:[@?]
1=!java.lang.IndexO...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.KeyedObjects2D_Zest_Test.test31(KeyedObjects2D_Zest_Test.java:224)
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
--- org.jfree.data.KeyedObjects2D_Zest_Test::test41
junit.framework.AssertionFailedError: expected:<...yedObjects2D@367
```

- Suite: `/home/pilaiphon/suites/Chart/zest/22/t12001/Chart-22f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_22/b120_r1/src/org/jfree/data/KeyedObjects2D_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_22/b120_r1`
