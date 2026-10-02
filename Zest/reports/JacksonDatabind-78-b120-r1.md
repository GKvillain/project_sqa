# Zest – JacksonDatabind-78 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 514628 (valid 5.93%) |
| Cycles | 60 |
| Corpus | 13 |
| Zest branch coverage (total / valid) | 127 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 13 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 32 / 354 = 9.04% |
| Branch coverage | 3 / 212 = 1.42% |
| Test suite length (statements) | 13 |
| Mutation score | 220 / 232 = 94.83% |
| Fuzz time | 126s |
| Pipeline time | 230s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.deser.BeanDeserializerFactory_Zest_Test::test8

```
--- com.fasterxml.jackson.databind.deser.BeanDeserializerFactory_Zest_Test::test8
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.BeanDeserializerFactory_Zest_Test.test8(BeanDeserializerFactory_Zest_Test.java:63)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/78/t12001/JacksonDatabind-78f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_78/b120_r1/src/com/fasterxml/jackson/databind/deser/BeanDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_78/b120_r1`
