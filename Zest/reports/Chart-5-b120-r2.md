# Zest – Chart-5 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 107977 (valid 81.07%) |
| Cycles | 7 |
| Corpus | 59 |
| Zest branch coverage (total / valid) | 280 / 273 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 59 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 86 / 175 = 49.14% |
| Branch coverage | 32 / 80 = 40.00% |
| Test suite length (statements) | 59 |
| Mutation score | 191 / 300 = 63.67% |
| Fuzz time | 120s |
| Pipeline time | 274s |

## Failing tests on buggy version

- org.jfree.data.xy.XYSeries_Zest_Test::test33
- org.jfree.data.xy.XYSeries_Zest_Test::test43

```
--- org.jfree.data.xy.XYSeries_Zest_Test::test33
junit.framework.AssertionFailedError: expected:<...ta.xy.XYSeries:@?
1=[null |this=org.jfree.data.xy.XYSeries:@?]
> but was:<...ta.xy.XYSeries:@?
1=[!java.lang.IndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.xy.XYSeries_Zest_Test.test33(XYSeries_Zest_Test.java:238)
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
--- org.jfree.data.xy.XYSeries_Zest_Test::test43
junit.framework.AssertionFailedError: expected:<...ta.xy.XYSeries:@?
1=[null |this=org.jfree.data.xy.XYSeries:@?]
> but was:<...ta.xy.XYSeries:@?
1=[!java.lang.IndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.xy.XYSeries_Zest_Test.test43(XYSeries_Zest_Test.java:308)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/5/t12002/Chart-5f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_5/b120_r2/src/org/jfree/data/xy/XYSeries_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_5/b120_r2`
