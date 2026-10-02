# Zest – Mockito-30 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 382067 (valid 0.00%) |
| Cycles | 38 |
| Corpus | 19 |
| Zest branch coverage (total / valid) | 175 / 0 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 19 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 24 / 112 = 21.43% |
| Branch coverage | 1 / 14 = 7.14% |
| Test suite length (statements) | 19 |
| Mutation score | 20 / 21 = 95.24% |
| Fuzz time | 120s |
| Pipeline time | 285s |

## Failing tests on buggy version

- org.mockito.exceptions.Reporter_Zest_Test::test10

```
--- org.mockito.exceptions.Reporter_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.exceptions.Reporter_Zest_Test.test10(Reporter_Zest_Test.java:77)
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

- Suite: `/home/user/suites/Mockito/zest/30/t12001/Mockito-30f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_30/b120_r1/src/org/mockito/exceptions/Reporter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_30/b120_r1`
