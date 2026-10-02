# Zest – JacksonCore-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 390115 (valid 92.81%) |
| Cycles | 30 |
| Corpus | 86 |
| Zest branch coverage (total / valid) | 206 / 203 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 86 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 73 / 84 = 86.90% |
| Branch coverage | 45 / 62 = 72.58% |
| Test suite length (statements) | 86 |
| Mutation score | 78 / 154 = 50.65% |
| Fuzz time | 128s |
| Pipeline time | 192s |

## Failing tests on buggy version

- com.fasterxml.jackson.core.JsonPointer_Zest_Test::test52
- com.fasterxml.jackson.core.JsonPointer_Zest_Test::test59
- com.fasterxml.jackson.core.JsonPointer_Zest_Test::test63

```
--- com.fasterxml.jackson.core.JsonPointer_Zest_Test::test52
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:-1]
> but was:<0=[!java.lang.NumberFormatException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.JsonPointer_Zest_Test.test52(JsonPointer_Zest_Test.java:371)
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
--- com.fasterxml.jackson.core.JsonPointer_Zest_Test::test59
junit.framework.AssertionFailedError: expected:<0=[java.lang.Integer:-1
1=java.lang.Integer:-1]
> but was:<0=[!java.lang.NumberFormatException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.JsonPointer_Zest_Test.test59(JsonPointer_Zest_Test.java:420)
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
--- com.fasterxml.jackson.core.JsonPointer_Zest_Test::test63
```

- Suite: `/home/user/suites/JacksonCore/zest/5/t12001/JacksonCore-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_5/b120_r1/src/com/fasterxml/jackson/core/JsonPointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_5/b120_r1`
