# Zest – JacksonDatabind-111 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 422874 (valid 16.12%) |
| Cycles | 48 |
| Corpus | 15 |
| Zest branch coverage (total / valid) | 116 / 97 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 15 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 9 / 351 = 2.56% |
| Branch coverage | 0 / 106 = 0.00% |
| Test suite length (statements) | 15 |
| Mutation score | 130 / 130 = 100.00% |
| Fuzz time | 125s |
| Pipeline time | 268s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.deser.CreatorProperty_Zest_Test::test13
- com.fasterxml.jackson.databind.deser.CreatorProperty_Zest_Test::test4

```
--- com.fasterxml.jackson.databind.deser.CreatorProperty_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[]!java.lang.NullPoint...> but was:<0=[java.util.concurrent.atomic.AtomicReference:null |this=com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer:@?
1=]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.CreatorProperty_Zest_Test.test13(CreatorProperty_Zest_Test.java:98)
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
--- com.fasterxml.jackson.databind.deser.CreatorProperty_Zest_Test::test4
junit.framework.AssertionFailedError: expected:<0=[]!java.lang.NullPoint...> but was:<0=[java.util.concurrent.atomic.AtomicReference:null |this=com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer:@?
1=]!java.lang.NullPoint...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.deser.CreatorProperty_Zest_Test.test4(CreatorProperty_Zest_Test.java:35)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/111/t12001/JacksonDatabind-111f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_111/b120_r1/src/com/fasterxml/jackson/databind/deser/CreatorProperty_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_111/b120_r1`
