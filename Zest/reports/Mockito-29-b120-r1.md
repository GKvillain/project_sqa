# Zest – Mockito-29 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 345444 (valid 63.10%) |
| Cycles | 105 |
| Corpus | 13 |
| Zest branch coverage (total / valid) | 146 / 143 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 13 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 14 / 15 = 93.33% |
| Branch coverage | 7 / 8 = 87.50% |
| Test suite length (statements) | 13 |
| Mutation score | 9 / 9 = 100.00% |
| Fuzz time | 120s |
| Pipeline time | 288s |

## Failing tests on buggy version

- org.mockito.internal.matchers.Same_Zest_Test::test10
- org.mockito.internal.matchers.Same_Zest_Test::test11
- org.mockito.internal.matchers.Same_Zest_Test::test3
- org.mockito.internal.matchers.Same_Zest_Test::test5
- org.mockito.internal.matchers.Same_Zest_Test::test7
- org.mockito.internal.matchers.Same_Zest_Test::test9

```
--- org.mockito.internal.matchers.Same_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...ernal.matchers.Same:[same(null)
1=java.lang.Boolean:true |this=org.mockito.internal.matchers.Same:same(null)]
> but was:<...ernal.matchers.Same:[!toString:java.lang.NullPointerException
1=java.lang.Boolean:true |this=org.mockito.internal.matchers.Same:!toString:java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.matchers.Same_Zest_Test.test10(Same_Zest_Test.java:77)
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
--- org.mockito.internal.matchers.Same_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...ernal.matchers.Same:[same(null)
1=new org.mockito.internal.matchers.Same:same(null)]
> but was:<...ernal.matchers.Same:[!toString:java.lang.NullPointerException
1=new org.mockito.internal.matchers.Same:!toString:java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.matchers.Same_Zest_Test.test11(Same_Zest_Test.java:84)
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
```

- Suite: `/home/user/suites/Mockito/zest/29/t12001/Mockito-29f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_29/b120_r1/src/org/mockito/internal/matchers/Same_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_29/b120_r1`
