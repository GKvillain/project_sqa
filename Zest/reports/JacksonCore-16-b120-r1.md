# Zest – JacksonCore-16 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 422914 (valid 93.24%) |
| Cycles | 46 |
| Corpus | 31 |
| Zest branch coverage (total / valid) | 174 / 171 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 31 |
| Failing on fixed | 0 |
| Failing on buggy | 18 |
| Bug detection | Fail (triggering: 18) |
| **Fault detected** | **yes** |
| Line coverage | 4 / 38 = 10.53% |
| Branch coverage | 2 / 26 = 7.69% |
| Test suite length (statements) | 31 |
| Mutation score | 41 / 45 = 91.11% |
| Fuzz time | 129s |
| Pipeline time | 191s |

## Failing tests on buggy version

- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test11
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test12
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test13
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test14
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test15
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test17
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test19
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test20
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test21
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test22
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test23
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test24
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test25
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test27
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test28
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test29
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test8
- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test9

```
--- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
> but was:<0=[void |this=com.fasterxml.jackson.core.util.JsonParserSequence:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test.test11(JsonParserSequence_Zest_Test.java:84)
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
--- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
> but was:<0=[com.fasterxml.jackson.core.util.JsonParserSequence:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test.test12(JsonParserSequence_Zest_Test.java:91)
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
--- com.fasterxml.jackson.core.util.JsonParserSequence_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[!java.lang.NullPointerException]
```

- Suite: `/home/user/suites/JacksonCore/zest/16/t12001/JacksonCore-16f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonCore_16/b120_r1/src/com/fasterxml/jackson/core/util/JsonParserSequence_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonCore_16/b120_r1`
