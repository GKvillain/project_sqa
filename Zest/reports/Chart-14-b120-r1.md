# Zest – Chart-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 139017 (valid 74.12%) |
| Cycles | 11 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 351 / 348 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 30 |
| Bug detection | Fail (triggering: 30) |
| **Fault detected** | **yes** |
| Line coverage | 237 / 2730 = 8.68% |
| Branch coverage | 48 / 1444 = 3.32% |
| Test suite length (statements) | 38 |
| Mutation score | 2708 / 2974 = 91.06% |
| Fuzz time | 120s |
| Pipeline time | 332s |

## Failing tests on buggy version

- org.jfree.chart.plot.CategoryPlot_Zest_Test::test10
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test11
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test12
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test14
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test16
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test18
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test19
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test21
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test23
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test24
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test25
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test26
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test27
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test28
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test29
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test30
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test32
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test33
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test34
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test35
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test36
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test37
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test0
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test1
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test3
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test4
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test5
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test7
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test8
- org.jfree.chart.plot.CategoryPlot_Zest_Test::test9

```
--- org.jfree.chart.plot.CategoryPlot_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.jfree.chart.plot.CategoryPlot:@?
1=!java.lang.IllegalArgument]Exception
> but was:<0=[!java.lang.NullPointer]Exception
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
--- org.jfree.chart.plot.CategoryPlot_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.jfree.chart.plot.XYPlot:@?
1=!java.lang.IllegalArgument]Exception
> but was:<0=[!java.lang.NullPointer]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.plot.CategoryPlot_Zest_Test.test11(CategoryPlot_Zest_Test.java:84)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/14/t12001/Chart-14f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_14/b120_r1/src/org/jfree/chart/plot/CategoryPlot_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_14/b120_r1`
