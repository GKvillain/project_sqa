# Zest – JacksonDatabind-53 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 526067 (valid 62.34%) |
| Cycles | 33 |
| Corpus | 89 |
| Zest branch coverage (total / valid) | 450 / 447 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 89 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 200 / 532 = 37.59% |
| Branch coverage | 112 / 358 = 31.28% |
| Test suite length (statements) | 95 |
| Mutation score | 405 / 519 = 78.03% |
| Fuzz time | 127s |
| Pipeline time | 254s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test::test14
- com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test::test56
- com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test::test1

```
--- com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test.test14(TypeBindings_Zest_Test.java:105)
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
--- com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test::test56
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test.test56(TypeBindings_Zest_Test.java:399)
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
--- com.fasterxml.jackson.databind.type.TypeBindings_Zest_Test::test1
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
```

- Suite: `/home/user/suites/JacksonDatabind/zest/53/t12001/JacksonDatabind-53f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_53/b120_r1/src/com/fasterxml/jackson/databind/type/TypeBindings_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_53/b120_r1`
