# Zest – JacksonDatabind-30 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 311367 (valid 76.08%) |
| Cycles | 24 |
| Corpus | 65 |
| Zest branch coverage (total / valid) | 1348 / 1345 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 65 |
| Failing on fixed | 0 |
| Failing on buggy | 14 |
| Bug detection | Fail (triggering: 14) |
| **Fault detected** | **yes** |
| Line coverage | 176 / 1342 = 13.11% |
| Branch coverage | 45 / 544 = 8.27% |
| Test suite length (statements) | 65 |
| Mutation score | 921 / 1024 = 89.94% |
| Fuzz time | 128s |
| Pipeline time | 264s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test10
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test11
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test12
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test23
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test35
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test36
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test37
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test39
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test41
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test45
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test0
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test1
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test5
- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test8

```
--- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<....ObjectMapper:@?
1=![java.lang.NullPointer]Exception
> but was:<....ObjectMapper:@?
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ObjectMapper_Zest_Test.test10(ObjectMapper_Zest_Test.java:77)
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
--- com.fasterxml.jackson.databind.ObjectMapper_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...:[TokenBuffer: ]
1=![java.lang.NullPointer]Exception
> but was:<...:[TokenBuffer: ]
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ObjectMapper_Zest_Test.test11(ObjectMapper_Zest_Test.java:84)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/30/t12001/JacksonDatabind-30f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_30/b120_r1/src/com/fasterxml/jackson/databind/ObjectMapper_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_30/b120_r1`
