# Zest – Chart-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 163044 (valid 84.89%) |
| Cycles | 12 |
| Corpus | 52 |
| Zest branch coverage (total / valid) | 280 / 273 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 52 |
| Failing on fixed | 0 |
| Failing on buggy | 10 |
| Bug detection | Fail (triggering: 10) |
| **Fault detected** | **yes** |
| Line coverage | 86 / 175 = 49.14% |
| Branch coverage | 32 / 80 = 40.00% |
| Test suite length (statements) | 52 |
| Mutation score | 194 / 300 = 64.67% |
| Fuzz time | 120s |
| Pipeline time | 298s |

## Failing tests on buggy version

- org.jfree.data.xy.XYSeries_Zest_Test::test12
- org.jfree.data.xy.XYSeries_Zest_Test::test16
- org.jfree.data.xy.XYSeries_Zest_Test::test23
- org.jfree.data.xy.XYSeries_Zest_Test::test26
- org.jfree.data.xy.XYSeries_Zest_Test::test29
- org.jfree.data.xy.XYSeries_Zest_Test::test36
- org.jfree.data.xy.XYSeries_Zest_Test::test0
- org.jfree.data.xy.XYSeries_Zest_Test::test3
- org.jfree.data.xy.XYSeries_Zest_Test::test7
- org.jfree.data.xy.XYSeries_Zest_Test::test8

```
--- org.jfree.data.xy.XYSeries_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<...ta.xy.XYSeries:@?
1=[null |this=org.jfree.data.xy.XYSeries:@?]
> but was:<...ta.xy.XYSeries:@?
1=[!java.lang.IndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.xy.XYSeries_Zest_Test.test12(XYSeries_Zest_Test.java:91)
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
--- org.jfree.data.xy.XYSeries_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<...ta.xy.XYSeries:@?
1=[null |this=org.jfree.data.xy.XYSeries:@?]
> but was:<...ta.xy.XYSeries:@?
1=[!java.lang.IndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.xy.XYSeries_Zest_Test.test16(XYSeries_Zest_Test.java:119)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/5/t12001/Chart-5f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_5/b120_r1/src/org/jfree/data/xy/XYSeries_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_5/b120_r1`
