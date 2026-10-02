# Zest – JacksonDatabind-61 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 465375 (valid 58.56%) |
| Cycles | 49 |
| Corpus | 33 |
| Zest branch coverage (total / valid) | 626 / 623 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 33 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 63 / 743 = 8.48% |
| Branch coverage | 9 / 248 = 3.63% |
| Test suite length (statements) | 33 |
| Mutation score | 354 / 377 = 93.90% |
| Fuzz time | 127s |
| Pipeline time | 229s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test20
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test29
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test3
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test7
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test9

```
--- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test20
junit.framework.AssertionFailedError: expected:<...per:@?
1=!java.lang.[NullPointer]Exception
> but was:<...per:@?
1=!java.lang.[IllegalState]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ObjectMapper_Zest_Test.test20(ObjectMapper_Zest_Test.java:147)
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
--- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test29
junit.framework.AssertionFailedError: expected:<...per:@?
1=!java.lang.[NullPointer]Exception
> but was:<...per:@?
1=!java.lang.[IllegalState]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ObjectMapper_Zest_Test.test29(ObjectMapper_Zest_Test.java:210)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/61/t12001/JacksonDatabind-61f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_61/b120_r1/src/com/fasterxml/jackson/databind/ObjectMapper_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_61/b120_r1`
