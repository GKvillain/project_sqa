# Zest – Chart-17 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 160367 (valid 54.65%) |
| Cycles | 7 |
| Corpus | 56 |
| Zest branch coverage (total / valid) | 240 / 233 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 56 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 60 / 289 = 20.76% |
| Branch coverage | 21 / 128 = 16.41% |
| Test suite length (statements) | 56 |
| Mutation score | 425 / 523 = 81.26% |
| Fuzz time | 120s |
| Pipeline time | 329s |

## Failing tests on buggy version

- org.jfree.data.time.TimeSeries_Zest_Test::test11
- org.jfree.data.time.TimeSeries_Zest_Test::test16
- org.jfree.data.time.TimeSeries_Zest_Test::test24
- org.jfree.data.time.TimeSeries_Zest_Test::test26
- org.jfree.data.time.TimeSeries_Zest_Test::test44
- org.jfree.data.time.TimeSeries_Zest_Test::test47
- org.jfree.data.time.TimeSeries_Zest_Test::test54

```
--- org.jfree.data.time.TimeSeries_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[org.jfree.data.time.TimeSeries:@? |this=org.jfree.data.time.TimeSeries:@?
1=]!java.lang.IllegalAr...> but was:<0=[]!java.lang.IllegalAr...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.time.TimeSeries_Zest_Test.test11(TimeSeries_Zest_Test.java:84)
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
--- org.jfree.data.time.TimeSeries_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[org.jfree.data.time.TimeSeries:@? |this=org.jfree.data.time.TimeSeries:@?
1=]!java.lang.IllegalAr...> but was:<0=[]!java.lang.IllegalAr...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.time.TimeSeries_Zest_Test.test16(TimeSeries_Zest_Test.java:119)
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
--- org.jfree.data.time.TimeSeries_Zest_Test::test24
junit.framework.AssertionFailedError: expected:<...ime.TimeSeries:@?
1=[org.jfree.data.time.TimeSeries:@? |this=org.jfree.data.time.TimeSeries:@?]
> but was:<...ime.TimeSeries:@?
```

- Suite: `/home/pilaiphon/suites/Chart/zest/17/t12001/Chart-17f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_17/b120_r1/src/org/jfree/data/time/TimeSeries_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_17/b120_r1`
