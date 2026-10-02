# Zest – JacksonDatabind-68 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 378449 (valid 100.00%) |
| Cycles | 130 |
| Corpus | 5 |
| Zest branch coverage (total / valid) | 98 / 98 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 5 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 1 / 593 = 0.17% |
| Branch coverage | 0 / 369 = 0.00% |
| Test suite length (statements) | 5 |
| Mutation score | 432 / 432 = 100.00% |
| Fuzz time | 124s |
| Pipeline time | 242s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.deser.BeanDeserializerBase_Zest_Test::test2

```
--- com.fasterxml.jackson.databind.deser.BeanDeserializerBase_Zest_Test::test2
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.BeanDeserializerBase_Zest_Test.test2(BeanDeserializerBase_Zest_Test.java:21)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/68/t12001/JacksonDatabind-68f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_68/b120_r1/src/com/fasterxml/jackson/databind/deser/BeanDeserializerBase_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_68/b120_r1`
