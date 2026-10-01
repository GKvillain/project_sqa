# Zest – Chart-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 137697 (valid 89.92%) |
| Cycles | 13 |
| Corpus | 47 |
| Zest branch coverage (total / valid) | 149 / 146 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 47 |
| Failing on fixed | 0 |
| Failing on buggy | 21 |
| Bug detection | Fail (triggering: 21) |
| **Fault detected** | **yes** |
| Line coverage | 3 / 3 = 100.00% |
| Branch coverage | 0 / 0 = 0% |
| Test suite length (statements) | 81 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 120s |
| Pipeline time | 269s |

## Failing tests on buggy version


```
--- org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[java.lang.String: title="null" alt="" |this=org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator_Zest_Test.test17(StandardToolTipTagFragmentGenerator_Zest_Test.java:126)
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
--- org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator_Zest_Test::test28
junit.framework.AssertionFailedError: expected:<...gmentGenerator:@?
1=[!java.lang.IllegalArgumentException]
> but was:<...gmentGenerator:@?
1=[java.lang.String: title="null" alt="" |this=org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator_Zest_Test.test28(StandardToolTipTagFragmentGenerator_Zest_Test.java:203)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/10/t12001/Chart-10f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_10/b120_r1/src/org/jfree/chart/imagemap/StandardToolTipTagFragmentGenerator_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_10/b120_r1`
