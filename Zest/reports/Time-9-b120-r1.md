# Zest – Time-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 375348 (valid 92.90%) |
| Cycles | 17 |
| Corpus | 219 |
| Zest branch coverage (total / valid) | 863 / 746 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 219 |
| Failing on fixed | 0 |
| Failing on buggy | 11 |
| Bug detection | Fail (triggering: 11) |
| **Fault detected** | **yes** |
| Line coverage | 230 / 355 = 64.79% |
| Branch coverage | 102 / 178 = 57.30% |
| Test suite length (statements) | 219 |
| Mutation score | 353 / 523 = 67.50% |
| Fuzz time | 123s |
| Pipeline time | 244s |

## Failing tests on buggy version

- org.joda.time.DateTimeZone_Zest_Test::test120
- org.joda.time.DateTimeZone_Zest_Test::test121
- org.joda.time.DateTimeZone_Zest_Test::test195
- org.joda.time.DateTimeZone_Zest_Test::test218
- org.joda.time.DateTimeZone_Zest_Test::test60
- org.joda.time.DateTimeZone_Zest_Test::test61
- org.joda.time.DateTimeZone_Zest_Test::test69
- org.joda.time.DateTimeZone_Zest_Test::test71
- org.joda.time.DateTimeZone_Zest_Test::test74
- org.joda.time.DateTimeZone_Zest_Test::test81
- org.joda.time.DateTimeZone_Zest_Test::test4

```
--- org.joda.time.DateTimeZone_Zest_Test::test120
junit.framework.AssertionFailedError: expected:<...erica/Los_Angeles
1=[!java.lang.IllegalArgumentException]
> but was:<...erica/Los_Angeles
1=[org.joda.time.tz.FixedDateTimeZone:+40:00]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.joda.time.DateTimeZone_Zest_Test.test120(DateTimeZone_Zest_Test.java:847)
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
--- org.joda.time.DateTimeZone_Zest_Test::test121
junit.framework.AssertionFailedError: expected:<...erica/Los_Angeles
1=[!java.lang.IllegalArgumentException]
> but was:<...erica/Los_Angeles
1=[org.joda.time.tz.FixedDateTimeZone:+58:00]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.joda.time.DateTimeZone_Zest_Test.test121(DateTimeZone_Zest_Test.java:854)
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

- Suite: `/home/user/suites/Time/zest/9/t12001/Time-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_9/b120_r1/src/org/joda/time/DateTimeZone_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_9/b120_r1`
