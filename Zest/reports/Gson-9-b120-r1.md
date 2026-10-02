# Zest – Gson-9 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 397497 (valid 84.67%) |
| Cycles | 33 |
| Corpus | 72 |
| Zest branch coverage (total / valid) | 288 / 285 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 72 |
| Failing on fixed | 0 |
| Failing on buggy | 11 |
| Bug detection | Fail (triggering: 11) |
| **Fault detected** | **yes** |
| Line coverage | 294 / 784 = 37.50% |
| Branch coverage | 75 / 338 = 22.19% |
| Test suite length (statements) | 72 |
| Mutation score | 501 / 632 = 79.27% |
| Fuzz time | 128s |
| Pipeline time | 201s |

## Failing tests on buggy version

- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test11
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test12
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test16
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test17
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test21
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test27
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test34
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test2
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test4
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test5
- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test7

```
--- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...am.JsonWriter:@?
1=![java.lang.IllegalState]Exception
> but was:<...am.JsonWriter:@?
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.internal.bind.JsonTreeWriter_Zest_Test.test11(JsonTreeWriter_Zest_Test.java:84)
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
--- com.google.gson.internal.bind.JsonTreeWriter_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<...JsonTreeWriter:@?
1=[com.google.gson.stream.JsonWriter:@? |this=com.google.gson.stream.JsonWriter:@?]
> but was:<...JsonTreeWriter:@?
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.internal.bind.JsonTreeWriter_Zest_Test.test12(JsonTreeWriter_Zest_Test.java:91)
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

- Suite: `/home/user/suites/Gson/zest/9/t12001/Gson-9f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_9/b120_r1/src/com/google/gson/internal/bind/JsonTreeWriter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_9/b120_r1`
