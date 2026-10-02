# Zest – Gson-1 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 341683 (valid 83.75%) |
| Cycles | 32 |
| Corpus | 38 |
| Zest branch coverage (total / valid) | 186 / 183 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 38 |
| Failing on fixed | 0 |
| Failing on buggy | 14 |
| Bug detection | Fail (triggering: 14) |
| **Fault detected** | **yes** |
| Line coverage | 26 / 79 = 32.91% |
| Branch coverage | 10 / 44 = 22.73% |
| Test suite length (statements) | 96 |
| Mutation score | 31 / 35 = 88.57% |
| Fuzz time | 128s |
| Pipeline time | 185s |

## Failing tests on buggy version

- com.google.gson.TypeInfoFactory_Zest_Test::test13
- com.google.gson.TypeInfoFactory_Zest_Test::test14
- com.google.gson.TypeInfoFactory_Zest_Test::test16
- com.google.gson.TypeInfoFactory_Zest_Test::test18
- com.google.gson.TypeInfoFactory_Zest_Test::test21
- com.google.gson.TypeInfoFactory_Zest_Test::test31
- com.google.gson.TypeInfoFactory_Zest_Test::test37
- com.google.gson.TypeInfoFactory_Zest_Test::test0
- com.google.gson.TypeInfoFactory_Zest_Test::test1
- com.google.gson.TypeInfoFactory_Zest_Test::test2
- com.google.gson.TypeInfoFactory_Zest_Test::test4
- com.google.gson.TypeInfoFactory_Zest_Test::test6
- com.google.gson.TypeInfoFactory_Zest_Test::test8
- com.google.gson.TypeInfoFactory_Zest_Test::test9

```
--- com.google.gson.TypeInfoFactory_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[null
1=null]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.TypeInfoFactory_Zest_Test.test13(TypeInfoFactory_Zest_Test.java:98)
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
--- com.google.gson.TypeInfoFactory_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<...ss:java.util.List
1=[null]
> but was:<...ss:java.util.List
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.TypeInfoFactory_Zest_Test.test14(TypeInfoFactory_Zest_Test.java:105)
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
```

- Suite: `/home/user/suites/Gson/zest/1/t12001/Gson-1f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_1/b120_r1/src/com/google/gson/TypeInfoFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_1/b120_r1`
