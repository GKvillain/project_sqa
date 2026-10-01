# Zest – Chart-8 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 119972 (valid 81.06%) |
| Cycles | 7 |
| Corpus | 70 |
| Zest branch coverage (total / valid) | 235 / 232 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 70 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 82 / 162 = 50.62% |
| Branch coverage | 27 / 68 = 39.71% |
| Test suite length (statements) | 70 |
| Mutation score | 250 / 302 = 82.78% |
| Fuzz time | 120s |
| Pipeline time | 267s |

## Failing tests on buggy version

- org.jfree.data.time.Week_Zest_Test::test26
- org.jfree.data.time.Week_Zest_Test::test40
- org.jfree.data.time.Week_Zest_Test::test41
- org.jfree.data.time.Week_Zest_Test::test42
- org.jfree.data.time.Week_Zest_Test::test48

```
--- org.jfree.data.time.Week_Zest_Test::test26
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[new org.jfree.data.time.Week:Week 53, -2320]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.time.Week_Zest_Test.test26(Week_Zest_Test.java:189)
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
--- org.jfree.data.time.Week_Zest_Test::test40
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
> but was:<0=[new org.jfree.data.time.Week:Week 49, -31314]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.time.Week_Zest_Test.test40(Week_Zest_Test.java:287)
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
--- org.jfree.data.time.Week_Zest_Test::test41
junit.framework.AssertionFailedError: expected:<0=[!java.lang.IllegalArgumentException]
```

- Suite: `/home/pilaiphon/suites/Chart/zest/8/t12002/Chart-8f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_8/b120_r2/src/org/jfree/data/time/Week_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_8/b120_r2`
