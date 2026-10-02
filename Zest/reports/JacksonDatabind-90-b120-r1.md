# Zest – JacksonDatabind-90 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 451546 (valid 62.84%) |
| Cycles | 33 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 167 / 164 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 30 / 224 = 13.39% |
| Branch coverage | 13 / 124 = 10.48% |
| Test suite length (statements) | 26 |
| Mutation score | 129 / 160 = 80.62% |
| Fuzz time | 126s |
| Pipeline time | 229s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test::test11
- com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test::test3
- com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test::test9

```
--- com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test.test11(ValueInstantiator_Zest_Test.java:84)
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
--- com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test::test3
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test.test3(ValueInstantiator_Zest_Test.java:28)
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
--- com.fasterxml.jackson.databind.deser.ValueInstantiator_Zest_Test::test9
junit.framework.AssertionFailedError: expected:<...antiator$Base:@?
```

- Suite: `/home/user/suites/JacksonDatabind/zest/90/t12001/JacksonDatabind-90f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_90/b120_r1/src/com/fasterxml/jackson/databind/deser/ValueInstantiator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_90/b120_r1`
