# Zest – Chart-19 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 109174 (valid 68.16%) |
| Cycles | 6 |
| Corpus | 53 |
| Zest branch coverage (total / valid) | 351 / 348 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 53 |
| Failing on fixed | 0 |
| Failing on buggy | 18 |
| Bug detection | Fail (triggering: 18) |
| **Fault detected** | **yes** |
| Line coverage | 192 / 1140 = 16.84% |
| Branch coverage | 60 / 598 = 10.03% |
| Test suite length (statements) | 53 |
| Mutation score | 1034 / 1210 = 85.45% |
| Fuzz time | 120s |
| Pipeline time | 272s |

## Failing tests on buggy version

- org.jfree.chart.plot.CategoryPlot_Zest_Test::test10
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test18
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test21
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test28
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test32
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test33
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test36
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test37
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test40
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test46
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test0
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test2
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test3
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test4
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test5
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test6
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test7
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test8

```
--- org.jfree.chart.plot.CategoryPlot_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...t.CategoryPlot:@?
1=[!java.lang.IllegalArgumentException]
> but was:<...t.CategoryPlot:@?
1=[java.lang.Integer:0 |this=org.jfree.chart.plot.CategoryPlot:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.CategoryPlot_Zest_Test.test10(CategoryPlot_Zest_Test.java:77)
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
--- org.jfree.chart.plot.CategoryPlot_Zest_Test::test18
junit.framework.AssertionFailedError: expected:<...t.CategoryPlot:@?
1=[!java.lang.IllegalArgumentException]
> but was:<...t.CategoryPlot:@?
1=[java.lang.Integer:0 |this=org.jfree.chart.plot.CategoryPlot:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.CategoryPlot_Zest_Test.test18(CategoryPlot_Zest_Test.java:133)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/19/t12002/Chart-19f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_19/b120_r2/src/org/jfree/chart/plot/CategoryPlot_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_19/b120_r2`
