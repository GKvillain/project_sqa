# Zest – Gson-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 430045 (valid 75.79%) |
| Cycles | 64 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 130 / 127 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 8 |
| Bug detection | Fail (triggering: 8) |
| **Fault detected** | **yes** |
| Line coverage | 16 / 40 = 40.00% |
| Branch coverage | 4 / 4 = 100.00% |
| Test suite length (statements) | 17 |
| Mutation score | 14 / 15 = 93.33% |
| Fuzz time | 126s |
| Pipeline time | 196s |

## Failing tests on buggy version

- com.google.gson.internal.UnsafeAllocator_Zest_Test::test10
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test12
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test15
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test0
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test1
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test3
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test4
- com.google.gson.internal.UnsafeAllocator_Zest_Test::test5

```
--- com.google.gson.internal.UnsafeAllocator_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=![java.lang.UnsupportedOperation]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.internal.UnsafeAllocator_Zest_Test.test10(UnsafeAllocator_Zest_Test.java:77)
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
--- com.google.gson.internal.UnsafeAllocator_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<...r$1:@?
1=!java.lang.[UnsupportedOperation]Exception
> but was:<...r$1:@?
1=!java.lang.[reflect.InvocationTarget]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.internal.UnsafeAllocator_Zest_Test.test12(UnsafeAllocator_Zest_Test.java:91)
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

- Suite: `/home/user/suites/Gson/zest/8/t12001/Gson-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_8/b120_r1/src/com/google/gson/internal/UnsafeAllocator_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_8/b120_r1`
