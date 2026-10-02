# Zest – JacksonDatabind-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 496406 (valid 100.00%) |
| Cycles | 252 |
| Corpus | 6 |
| Zest branch coverage (total / valid) | 43 / 43 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 6 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 94 / 624 = 15.06% |
| Branch coverage | 28 / 308 = 9.09% |
| Test suite length (statements) | 6 |
| Mutation score | 541 / 614 = 88.11% |
| Fuzz time | 129s |
| Pipeline time | 228s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.util.TokenBuffer_Zest_Test::test3

```
--- com.fasterxml.jackson.databind.util.TokenBuffer_Zest_Test::test3
junit.framework.AssertionFailedError: expected:<...[TokenBuffer: VALUE_[NULL]]
> but was:<...[TokenBuffer: VALUE_[EMBEDDED_OBJECT]]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.util.TokenBuffer_Zest_Test.test3(TokenBuffer_Zest_Test.java:28)
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

- Suite: `/home/user/suites/JacksonDatabind/zest/2/t12001/JacksonDatabind-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_2/b120_r1/src/com/fasterxml/jackson/databind/util/TokenBuffer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_2/b120_r1`
