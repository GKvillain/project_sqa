# Zest – Chart-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 74844 (valid 62.73%) |
| Cycles | 4 |
| Corpus | 47 |
| Zest branch coverage (total / valid) | 351 / 348 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 47 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 190 / 1140 = 16.67% |
| Branch coverage | 59 / 598 = 9.87% |
| Test suite length (statements) | 48 |
| Mutation score | 1034 / 1210 = 85.45% |
| Fuzz time | 121s |
| Pipeline time | 345s |

## Failing tests on buggy version

- org.jfree.chart.plot.CategoryPlot_Zest_Test::test14
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test27
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test28
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test35

```
--- org.jfree.chart.plot.CategoryPlot_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[java.lang.Integer:0 |this=org.jfree.chart.plot.CategoryPlot:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.CategoryPlot_Zest_Test.test14(CategoryPlot_Zest_Test.java:105)
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
--- org.jfree.chart.plot.CategoryPlot_Zest_Test::test27
junit.framework.AssertionFailedError: expected:<...t.CategoryPlot:@?
1=[!java.lang.IllegalArgumentException]
> but was:<...t.CategoryPlot:@?
1=[java.lang.Integer:0 |this=org.jfree.chart.plot.CategoryPlot:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.CategoryPlot_Zest_Test.test27(CategoryPlot_Zest_Test.java:196)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/19/t12001/Chart-19f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_19/b120_r1/src/org/jfree/chart/plot/CategoryPlot_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_19/b120_r1`
