# Zest – JacksonCore-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 409602 (valid 96.37%) |
| Cycles | 25 |
| Corpus | 63 |
| Zest branch coverage (total / valid) | 192 / 189 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 63 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 47 / 72 = 65.28% |
| Branch coverage | 2 / 34 = 5.88% |
| Test suite length (statements) | 63 |
| Mutation score | 61 / 80 = 76.25% |
| Fuzz time | 127s |
| Pipeline time | 196s |

## Failing tests on buggy version

- com.fasterxml.jackson.core.json.JsonWriteContext_Zest_Test::test25
- com.fasterxml.jackson.core.json.JsonWriteContext_Zest_Test::test27

```
--- com.fasterxml.jackson.core.json.JsonWriteContext_Zest_Test::test25
junit.framework.AssertionFailedError: expected:<...1=java.lang.Integer:[5] |this=com.fasterxml...> but was:<...1=java.lang.Integer:[2] |this=com.fasterxml...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.json.JsonWriteContext_Zest_Test.test25(JsonWriteContext_Zest_Test.java:182)
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
--- com.fasterxml.jackson.core.json.JsonWriteContext_Zest_Test::test27
junit.framework.AssertionFailedError: expected:<0=java.lang.Integer:[5 |this=com.fasterxml.jackson.core.json.JsonWriteContext:{?}
1=java.lang.Integer:5] |this=com.fasterxml...> but was:<0=java.lang.Integer:[2 |this=com.fasterxml.jackson.core.json.JsonWriteContext:{?}
1=java.lang.Integer:2] |this=com.fasterxml...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.json.JsonWriteContext_Zest_Test.test27(JsonWriteContext_Zest_Test.java:196)
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

- Suite: `/home/user/suites/JacksonCore/zest/7/t12001/JacksonCore-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_7/b120_r1/src/com/fasterxml/jackson/core/json/JsonWriteContext_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_7/b120_r1`
