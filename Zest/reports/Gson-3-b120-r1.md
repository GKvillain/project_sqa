# Zest – Gson-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 401541 (valid 78.79%) |
| Cycles | 35 |
| Corpus | 29 |
| Zest branch coverage (total / valid) | 164 / 161 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 29 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 24 / 94 = 25.53% |
| Branch coverage | 8 / 36 = 22.22% |
| Test suite length (statements) | 29 |
| Mutation score | 25 / 34 = 73.53% |
| Fuzz time | 128s |
| Pipeline time | 193s |

## Failing tests on buggy version

- com.google.gson.internal.ConstructorConstructor_Zest_Test::test17
- com.google.gson.internal.ConstructorConstructor_Zest_Test::test23
- com.google.gson.internal.ConstructorConstructor_Zest_Test::test24
- com.google.gson.internal.ConstructorConstructor_Zest_Test::test28
- com.google.gson.internal.ConstructorConstructor_Zest_Test::test6

```
--- com.google.gson.internal.ConstructorConstructor_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<...tructorConstructor$1[4]:@? |this=com.google...> but was:<...tructorConstructor$1[2]:@? |this=com.google...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.internal.ConstructorConstructor_Zest_Test.test17(ConstructorConstructor_Zest_Test.java:126)
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
--- com.google.gson.internal.ConstructorConstructor_Zest_Test::test23
junit.framework.AssertionFailedError: expected:<...tructorConstructor$1[4:@? |this=com.google.gson.internal.ConstructorConstructor:{}
1=com.google.gson.internal.ConstructorConstructor$14]:@? |this=com.google...> but was:<...tructorConstructor$1[2:@? |this=com.google.gson.internal.ConstructorConstructor:{}
1=com.google.gson.internal.ConstructorConstructor$12]:@? |this=com.google...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.google.gson.internal.ConstructorConstructor_Zest_Test.test23(ConstructorConstructor_Zest_Test.java:168)
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
--- com.google.gson.internal.ConstructorConstructor_Zest_Test::test24
junit.framework.AssertionFailedError: expected:<...tructorConstructor$1[4]:@? |this=com.google...> but was:<...tructorConstructor$1[2]:@? |this=com.google...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
```

- Suite: `/home/user/suites/Gson/zest/3/t12001/Gson-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Gson_3/b120_r1/src/com/google/gson/internal/ConstructorConstructor_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Gson_3/b120_r1`
