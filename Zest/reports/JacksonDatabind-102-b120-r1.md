# Zest – JacksonDatabind-102 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 393290 (valid 47.48%) |
| Cycles | 28 |
| Corpus | 30 |
| Zest branch coverage (total / valid) | 161 / 158 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 17 / 69 = 24.64% |
| Branch coverage | 7 / 50 = 14.00% |
| Test suite length (statements) | 30 |
| Mutation score | 54 / 58 = 93.10% |
| Fuzz time | 126s |
| Pipeline time | 246s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase_Zest_Test::test17
- com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase_Zest_Test::test4

```
--- com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<...ndarSerializer:@?
1=[!java.lang.NullPointerException]
> but was:<...ndarSerializer:@?
1=[com.fasterxml.jackson.databind.ser.std.CalendarSerializer:@? |this=com.fasterxml.jackson.databind.ser.std.CalendarSerializer:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase_Zest_Test.test17(DateTimeSerializerBase_Zest_Test.java:126)
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
--- com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase_Zest_Test::test4
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
> but was:<0=[com.fasterxml.jackson.databind.ser.std.CalendarSerializer:@? |this=com.fasterxml.jackson.databind.ser.std.CalendarSerializer:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase_Zest_Test.test4(DateTimeSerializerBase_Zest_Test.java:35)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/102/t12001/JacksonDatabind-102f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_102/b120_r1/src/com/fasterxml/jackson/databind/ser/std/DateTimeSerializerBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_102/b120_r1`
