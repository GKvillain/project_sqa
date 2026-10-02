# Zest – Mockito-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 314795 (valid 100.00%) |
| Cycles | 108 |
| Corpus | 6 |
| Zest branch coverage (total / valid) | 103 / 103 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 6 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 11 / 19 = 57.89% |
| Branch coverage | 3 / 10 = 30.00% |
| Test suite length (statements) | 6 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 122s |
| Pipeline time | 813s |

## Failing tests on buggy version

- org.mockito.internal.creation.DelegatingMethod_Zest_Test::test3

```
--- org.mockito.internal.creation.DelegatingMethod_Zest_Test::test3
junit.framework.AssertionFailedError: expected:<...on.DelegatingMethod:[!toString:java.lang.NullPointerException]
> but was:<...on.DelegatingMethod:[org.mockito.internal.creation.DelegatingMethod@1]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.creation.DelegatingMethod_Zest_Test.test3(DelegatingMethod_Zest_Test.java:28)
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

- Suite: `/home/user/suites/Mockito/zest/11/t12001/Mockito-11f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_11/b120_r1/src/org/mockito/internal/creation/DelegatingMethod_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_11/b120_r1`
