# Zest – Chart-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 151750 (valid 89.06%) |
| Cycles | 10 |
| Corpus | 65 |
| Zest branch coverage (total / valid) | 238 / 235 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 64 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 83 / 162 = 51.23% |
| Branch coverage | 30 / 68 = 44.12% |
| Test suite length (statements) | 64 |
| Mutation score | 259 / 302 = 85.76% |
| Fuzz time | 121s |
| Pipeline time | 286s |

## Failing tests on buggy version

- org.jfree.data.time.Week_Zest_Test::test14
- org.jfree.data.time.Week_Zest_Test::test16

```
--- org.jfree.data.time.Week_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<...ata.time.Week:Week 2[9], -408
> but was:<...ata.time.Week:Week 2[8], -408
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.time.Week_Zest_Test.test14(Week_Zest_Test.java:105)
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
--- org.jfree.data.time.Week_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<...eek:Week 40, 2026
1=[!java.lang.IllegalArgumentException]
> but was:<...eek:Week 40, 2026
1=[new org.jfree.data.time.Week:Week 38, -29966]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jfree.data.time.Week_Zest_Test.test16(Week_Zest_Test.java:119)
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

- Suite: `/home/pilaiphon/suites/Chart/zest/8/t12001/Chart-8f-zest.12001.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Chart_8/b120_r1/src/org/jfree/data/time/Week_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Chart_8/b120_r1`
