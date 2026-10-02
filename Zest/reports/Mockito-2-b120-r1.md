# Zest – Mockito-2 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 369489 (valid 60.08%) |
| Cycles | 82 |
| Corpus | 12 |
| Zest branch coverage (total / valid) | 145 / 136 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 12 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 13 / 13 = 100.00% |
| Branch coverage | 4 / 8 = 50.00% |
| Test suite length (statements) | 12 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 123s |
| Pipeline time | 613s |

## Failing tests on buggy version

- org.mockito.internal.util.Timer_Zest_Test::test11
- org.mockito.internal.util.Timer_Zest_Test::test0
- org.mockito.internal.util.Timer_Zest_Test::test1
- org.mockito.internal.util.Timer_Zest_Test::test2
- org.mockito.internal.util.Timer_Zest_Test::test3
- org.mockito.internal.util.Timer_Zest_Test::test6

```
--- org.mockito.internal.util.Timer_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[!org.mockito.exceptions.misusing.FriendlyReminderException]
> but was:<0=[new org.mockito.internal.util.Timer:@?
1=void |this=org.mockito.internal.util.Timer:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.util.Timer_Zest_Test.test11(Timer_Zest_Test.java:84)
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
--- org.mockito.internal.util.Timer_Zest_Test::test0
junit.framework.AssertionFailedError: expected:<0=[!org.mockito.exceptions.misusing.FriendlyReminderException]
> but was:<0=[new org.mockito.internal.util.Timer:@?
1=void |this=org.mockito.internal.util.Timer:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.util.Timer_Zest_Test.test0(Timer_Zest_Test.java:7)
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

- Suite: `/home/user/suites/Mockito/zest/2/t12001/Mockito-2f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_2/b120_r1/src/org/mockito/internal/util/Timer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_2/b120_r1`
