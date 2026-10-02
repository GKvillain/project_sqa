# Zest – JacksonDatabind-77 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 442888 (valid 7.01%) |
| Cycles | 61 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 124 / 35 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 17 / 332 = 5.12% |
| Branch coverage | 1 / 206 = 0.49% |
| Test suite length (statements) | 12 |
| Mutation score | 220 / 223 = 98.65% |
| Fuzz time | 125s |
| Pipeline time | 227s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.deser.BeanDeserializerFactory_Zest_Test::test6

```
--- com.fasterxml.jackson.databind.deser.BeanDeserializerFactory_Zest_Test::test6
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.BeanDeserializerFactory_Zest_Test.test6(BeanDeserializerFactory_Zest_Test.java:49)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/77/t12001/JacksonDatabind-77f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_77/b120_r1/src/com/fasterxml/jackson/databind/deser/BeanDeserializerFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_77/b120_r1`
