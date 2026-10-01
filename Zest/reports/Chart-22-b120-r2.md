# Zest – Chart-22 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 130919 (valid 63.94%) |
| Cycles | 6 |
| Corpus | 58 |
| Zest branch coverage (total / valid) | 226 / 223 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 58 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 106 / 145 = 73.10% |
| Branch coverage | 41 / 74 = 55.41% |
| Test suite length (statements) | 58 |
| Mutation score | 114 / 174 = 65.52% |
| Fuzz time | 120s |
| Pipeline time | 260s |

## Failing tests on buggy version

- org.jfree.data.KeyedObjects2D_Zest_Test::test31
- org.jfree.data.KeyedObjects2D_Zest_Test::test32
- org.jfree.data.KeyedObjects2D_Zest_Test::test38
- org.jfree.data.KeyedObjects2D_Zest_Test::test39
- org.jfree.data.KeyedObjects2D_Zest_Test::test48
- org.jfree.data.KeyedObjects2D_Zest_Test::test5
- org.jfree.data.KeyedObjects2D_Zest_Test::test8

```
--- org.jfree.data.KeyedObjects2D_Zest_Test::test31
junit.framework.AssertionFailedError: expected:<...yedObjects2D@367
1=![org.jfree.data.UnknownKey]Exception
> but was:<...yedObjects2D@367
1=![java.lang.IndexOutOfBounds]Exception
>
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
--- org.jfree.data.KeyedObjects2D_Zest_Test::test32
junit.framework.AssertionFailedError: expected:<0=![org.jfree.data.UnknownKey]Exception
> but was:<0=![java.lang.IndexOutOfBounds]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.KeyedObjects2D_Zest_Test.test32(KeyedObjects2D_Zest_Test.java:231)
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
```

- Suite: `/home/pilaiphon/suites/Chart/zest/22/t12002/Chart-22f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_22/b120_r2/src/org/jfree/data/KeyedObjects2D_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_22/b120_r2`
