# Zest – JacksonDatabind-75 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 406341 (valid 64.66%) |
| Cycles | 75 |
| Corpus | 16 |
| Zest branch coverage (total / valid) | 138 / 135 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 16 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 6 / 69 = 8.70% |
| Branch coverage | 2 / 50 = 4.00% |
| Test suite length (statements) | 16 |
| Mutation score | 53 / 53 = 100.00% |
| Fuzz time | 127s |
| Pipeline time | 229s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test11
- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test12
- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test13
- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test14
- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test5
- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test8
- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test9

```
--- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[null
1=null]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test.test11(EnumSerializer_Zest_Test.java:84)
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
--- com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:true
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.std.EnumSerializer_Zest_Test.test12(EnumSerializer_Zest_Test.java:91)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/75/t12001/JacksonDatabind-75f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_75/b120_r1/src/com/fasterxml/jackson/databind/ser/std/EnumSerializer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_75/b120_r1`
