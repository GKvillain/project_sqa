# Zest – Chart-15 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 131978 (valid 55.91%) |
| Cycles | 7 |
| Corpus | 48 |
| Zest branch coverage (total / valid) | 395 / 392 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 48 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 97 / 839 = 11.56% |
| Branch coverage | 14 / 316 = 4.43% |
| Test suite length (statements) | 48 |
| Mutation score | 1231 / 1356 = 90.78% |
| Fuzz time | 120s |
| Pipeline time | 274s |

## Failing tests on buggy version

- org.jfree.chart.plot.PiePlot_Zest_Test::test12
- org.jfree.chart.plot.PiePlot_Zest_Test::test13
- org.jfree.chart.plot.PiePlot_Zest_Test::test16
- org.jfree.chart.plot.PiePlot_Zest_Test::test17
- org.jfree.chart.plot.PiePlot_Zest_Test::test29
- org.jfree.chart.plot.PiePlot_Zest_Test::test33
- org.jfree.chart.plot.PiePlot_Zest_Test::test8

```
--- org.jfree.chart.plot.PiePlot_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[java.lang.Double:0.0 |this=org.jfree.chart.plot.PiePlot:@?
1=]!java.lang.NullPoint...> but was:<0=[]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.PiePlot_Zest_Test.test12(PiePlot_Zest_Test.java:91)
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
--- org.jfree.chart.plot.PiePlot_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[java.lang.Double:0.0 |this=org.jfree.chart.plot.PiePlot:@?
1=]!java.lang.NullPoint...> but was:<0=[]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.PiePlot_Zest_Test.test13(PiePlot_Zest_Test.java:98)
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
--- org.jfree.chart.plot.PiePlot_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=!java.lang.[NullPointer]Exception
> but was:<0=!java.lang.[IllegalArgument]Exception
>
```

- Suite: `/home/pilaiphon/suites/Chart/zest/15/t12001/Chart-15f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_15/b120_r1/src/org/jfree/chart/plot/PiePlot_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_15/b120_r1`
