# Zest – JacksonDatabind-95 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 337673 (valid 62.69%) |
| Cycles | 15 |
| Corpus | 97 |
| Zest branch coverage (total / valid) | 516 / 513 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 97 |
| Failing on fixed | 0 |
| Failing on buggy | 17 |
| Bug detection | Fail (triggering: 17) |
| **Fault detected** | **yes** |
| Line coverage | 162 / 459 = 35.29% |
| Branch coverage | 88 / 271 = 32.47% |
| Test suite length (statements) | 170 |
| Mutation score | 252 / 312 = 80.77% |
| Fuzz time | 124s |
| Pipeline time | 280s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test26
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test33
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test34
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test35
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test41
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test43
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test45
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test63
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test64
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test65
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test66
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test73
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test91
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test93
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test94
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test95
- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test96

```
--- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test26
junit.framework.AssertionFailedError: expected:<0=!java.lang.[IllegalArgument]Exception
> but was:<0=!java.lang.[NullPointer]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test.test26(TypeFactory_Zest_Test.java:189)
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
--- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test33
junit.framework.AssertionFailedError: expected:<0=!java.lang.[IllegalArgument]Exception
> but was:<0=!java.lang.[NullPointer]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test.test33(TypeFactory_Zest_Test.java:238)
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
--- com.fasterxml.jackson.databind.type.TypeFactory_Zest_Test::test34
junit.framework.AssertionFailedError: expected:<0=!java.lang.[IllegalArgument]Exception
```

- Suite: `/home/user/suites/JacksonDatabind/zest/95/t12001/JacksonDatabind-95f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_95/b120_r1/src/com/fasterxml/jackson/databind/type/TypeFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_95/b120_r1`
