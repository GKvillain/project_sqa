# Zest – Chart-15 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 111557 (valid 51.32%) |
| Cycles | 5 |
| Corpus | 51 |
| Zest branch coverage (total / valid) | 395 / 392 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 10 |
| Bug detection | Fail (triggering: 10) |
| **Fault detected** | **yes** |
| Line coverage | 97 / 839 = 11.56% |
| Branch coverage | 14 / 316 = 4.43% |
| Test suite length (statements) | 51 |
| Mutation score | 1231 / 1356 = 90.78% |
| Fuzz time | 120s |
| Pipeline time | 274s |

## Failing tests on buggy version

- org.jfree.chart.plot.PiePlot_Zest_Test::test10
- org.jfree.chart.plot.PiePlot_Zest_Test::test13
- org.jfree.chart.plot.PiePlot_Zest_Test::test25
- org.jfree.chart.plot.PiePlot_Zest_Test::test31
- org.jfree.chart.plot.PiePlot_Zest_Test::test32
- org.jfree.chart.plot.PiePlot_Zest_Test::test34
- org.jfree.chart.plot.PiePlot_Zest_Test::test36
- org.jfree.chart.plot.PiePlot_Zest_Test::test37
- org.jfree.chart.plot.PiePlot_Zest_Test::test38
- org.jfree.chart.plot.PiePlot_Zest_Test::test40

```
--- org.jfree.chart.plot.PiePlot_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[java.lang.Double:0.0 |this=org.jfree.chart.plot.PiePlot:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.PiePlot_Zest_Test.test10(PiePlot_Zest_Test.java:77)
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
junit.framework.AssertionFailedError: expected:<0=[java.lang.Double:0.0 |this=org.jfree.chart.plot.PiePlot:@?]
> but was:<0=[!java.lang.NullPointerException]
>
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
--- org.jfree.chart.plot.PiePlot_Zest_Test::test25
junit.framework.AssertionFailedError: expected:<0=!java.lang.[NullPointer]Exception
```

- Suite: `/home/pilaiphon/suites/Chart/zest/15/t12002/Chart-15f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_15/b120_r2/src/org/jfree/chart/plot/PiePlot_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_15/b120_r2`
