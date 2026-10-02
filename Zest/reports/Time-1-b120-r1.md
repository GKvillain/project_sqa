# Zest – Time-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 364452 (valid 58.93%) |
| Cycles | 52 |
| Corpus | 49 |
| Zest branch coverage (total / valid) | 603 / 293 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 49 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 92 / 305 = 30.16% |
| Branch coverage | 27 / 122 = 22.13% |
| Test suite length (statements) | 61 |
| Mutation score | 367 / 407 = 90.17% |
| Fuzz time | 123s |
| Pipeline time | 233s |

## Failing tests on buggy version

- org.joda.time.field.UnsupportedDurationField_Zest_Test::test16
- org.joda.time.field.UnsupportedDurationField_Zest_Test::test21
- org.joda.time.field.UnsupportedDurationField_Zest_Test::test25

```
--- org.joda.time.field.UnsupportedDurationField_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:0 |this=org.joda.time.field.UnsupportedDurationField:!toString:java.lang.NullPointerException
1=new org.joda.time.Partial:[]]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.joda.time.field.UnsupportedDurationField_Zest_Test.test16(UnsupportedDurationField_Zest_Test.java:119)
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
--- org.joda.time.field.UnsupportedDurationField_Zest_Test::test21
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:0 |this=org.joda.time.field.UnsupportedDurationField:!toString:java.lang.NullPointerException
1=java.lang.Integer:0 |this=org.joda.time.field.UnsupportedDurationField:!toString:]java.lang.NullPointe...> but was:<0=[!]java.lang.NullPointe...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.joda.time.field.UnsupportedDurationField_Zest_Test.test21(UnsupportedDurationField_Zest_Test.java:154)
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
--- org.joda.time.field.UnsupportedDurationField_Zest_Test::test25
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:0 |this=org.joda.time.field.UnsupportedDurationField:!toString:java.lang.NullPointerException
```

- Suite: `/home/user/suites/Time/zest/1/t12001/Time-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_1/b120_r1/src/org/joda/time/field/UnsupportedDurationField_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_1/b120_r1`
