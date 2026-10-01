# Zest – Chart-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 25786 (valid 83.47%) |
| Cycles | 3 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 242 / 239 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 32 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 40 / 156 = 25.64% |
| Branch coverage | 8 / 56 = 14.29% |
| Test suite length (statements) | 32 |
| Mutation score | 181 / 224 = 80.80% |
| Fuzz time | 120s |
| Pipeline time | 273s |

## Failing tests on buggy version

- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test10
- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test15
- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test16
- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test27
- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test6

```
--- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...tegoryRenderer:@?
1=[java.lang.Boolean:false |this=org.jfree.chart.renderer.category.MinMaxCategoryRenderer:@?]
> but was:<...tegoryRenderer:@?
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test.test10(MinMaxCategoryRenderer_Zest_Test.java:77)
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
--- org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:false |this=org.jfree.chart.renderer.category.MinMaxCategoryRenderer:@?
1=!java.lang.IllegalArgument]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.chart.renderer.category.MinMaxCategoryRenderer_Zest_Test.test15(MinMaxCategoryRenderer_Zest_Test.java:112)
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
```

- Suite: `/home/pilaiphon/suites/Chart/zest/23/t12001/Chart-23f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_23/b120_r1/src/org/jfree/chart/renderer/category/MinMaxCategoryRenderer_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_23/b120_r1`
