# Zest – Gson-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 373071 (valid 82.31%) |
| Cycles | 27 |
| Corpus | 57 |
| Zest branch coverage (total / valid) | 214 / 207 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 57 |
| Failing on fixed | 0 |
| Failing on buggy | 51 |
| Bug detection | Fail (triggering: 51) |
| **Fault detected** | **yes** |
| Line coverage | 132 / 876 = 15.07% |
| Branch coverage | 39 / 557 = 7.00% |
| Test suite length (statements) | 57 |
| Mutation score | 1538 / 1651 = 93.16% |
| Fuzz time | 126s |
| Pipeline time | 202s |

## Failing tests on buggy version

- com.google.gson.stream.JsonReader_Zest_Test::test10
- com.google.gson.stream.JsonReader_Zest_Test::test11
- com.google.gson.stream.JsonReader_Zest_Test::test12
- com.google.gson.stream.JsonReader_Zest_Test::test13
- com.google.gson.stream.JsonReader_Zest_Test::test14
- com.google.gson.stream.JsonReader_Zest_Test::test15
- com.google.gson.stream.JsonReader_Zest_Test::test16
- com.google.gson.stream.JsonReader_Zest_Test::test17
- com.google.gson.stream.JsonReader_Zest_Test::test18
- com.google.gson.stream.JsonReader_Zest_Test::test19
- com.google.gson.stream.JsonReader_Zest_Test::test20
- com.google.gson.stream.JsonReader_Zest_Test::test21
- com.google.gson.stream.JsonReader_Zest_Test::test22
- com.google.gson.stream.JsonReader_Zest_Test::test23
- com.google.gson.stream.JsonReader_Zest_Test::test24
- com.google.gson.stream.JsonReader_Zest_Test::test25
- com.google.gson.stream.JsonReader_Zest_Test::test26
- com.google.gson.stream.JsonReader_Zest_Test::test28
- com.google.gson.stream.JsonReader_Zest_Test::test29
- com.google.gson.stream.JsonReader_Zest_Test::test30
- com.google.gson.stream.JsonReader_Zest_Test::test31
- com.google.gson.stream.JsonReader_Zest_Test::test32
- com.google.gson.stream.JsonReader_Zest_Test::test33
- com.google.gson.stream.JsonReader_Zest_Test::test34
- com.google.gson.stream.JsonReader_Zest_Test::test35
- com.google.gson.stream.JsonReader_Zest_Test::test36
- com.google.gson.stream.JsonReader_Zest_Test::test37
- com.google.gson.stream.JsonReader_Zest_Test::test38
- com.google.gson.stream.JsonReader_Zest_Test::test39
- com.google.gson.stream.JsonReader_Zest_Test::test40
- com.google.gson.stream.JsonReader_Zest_Test::test41
- com.google.gson.stream.JsonReader_Zest_Test::test42
- com.google.gson.stream.JsonReader_Zest_Test::test44
- com.google.gson.stream.JsonReader_Zest_Test::test45
- com.google.gson.stream.JsonReader_Zest_Test::test46
- com.google.gson.stream.JsonReader_Zest_Test::test47
- com.google.gson.stream.JsonReader_Zest_Test::test48
- com.google.gson.stream.JsonReader_Zest_Test::test49
- com.google.gson.stream.JsonReader_Zest_Test::test50
- com.google.gson.stream.JsonReader_Zest_Test::test51
- com.google.gson.stream.JsonReader_Zest_Test::test52
- com.google.gson.stream.JsonReader_Zest_Test::test53
- com.google.gson.stream.JsonReader_Zest_Test::test54
- com.google.gson.stream.JsonReader_Zest_Test::test55
- com.google.gson.stream.JsonReader_Zest_Test::test56
- com.google.gson.stream.JsonReader_Zest_Test::test0
- com.google.gson.stream.JsonReader_Zest_Test::test1
- com.google.gson.stream.JsonReader_Zest_Test::test2
- com.google.gson.stream.JsonReader_Zest_Test::test6
- com.google.gson.stream.JsonReader_Zest_Test::test7
- com.google.gson.stream.JsonReader_Zest_Test::test9

```
--- com.google.gson.stream.JsonReader_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[com.google.gson.stream.JsonWriter:@? |this=com.google.gson.stream.JsonWriter:@?]
> but was:<0=[!java.lang.IllegalStateException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.stream.JsonReader_Zest_Test.test10(JsonReader_Zest_Test.java:77)
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
--- com.google.gson.stream.JsonReader_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[void |this=com.google.gson.stream.JsonWriter:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.stream.JsonReader_Zest_Test.test11(JsonReader_Zest_Test.java:84)
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
--- com.google.gson.stream.JsonReader_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[com.google.gson.stream.JsonWriter:@? |this=com.google.gson.stream.JsonWriter:@?
```

- Suite: `/home/user/suites/Gson/zest/4/t12001/Gson-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_4/b120_r1/src/com/google/gson/stream/JsonReader_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_4/b120_r1`
