# Zest – JacksonDatabind-109 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 298473 (valid 82.75%) |
| Cycles | 66 |
| Corpus | 31 |
| Zest branch coverage (total / valid) | 974 / 971 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 31 |
| Failing on fixed | 0 |
| Failing on buggy | 8 |
| Bug detection | Fail (triggering: 8) |
| **Fault detected** | **yes** |
| Line coverage | 55 / 116 = 47.41% |
| Branch coverage | 17 / 52 = 32.69% |
| Test suite length (statements) | 31 |
| Mutation score | 46 / 82 = 56.10% |
| Fuzz time | 124s |
| Pipeline time | 268s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test16
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test17
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test20
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test22
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test28
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test0
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test1
- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test3

```
--- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.ser.std.NumberSerializer$BigDecimalAsStringSerializer:@?
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test.test16(NumberSerializer_Zest_Test.java:119)
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
--- com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=[com.fasterxml.jackson.databind.ser.std.NumberSerializer$BigDecimalAsStringSerializer:@?
1=new com.fasterxml.jackson.databind.ser.std.NumberSerializers:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.std.NumberSerializer_Zest_Test.test17(NumberSerializer_Zest_Test.java:126)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/109/t12001/JacksonDatabind-109f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_109/b120_r1/src/com/fasterxml/jackson/databind/ser/std/NumberSerializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_109/b120_r1`
