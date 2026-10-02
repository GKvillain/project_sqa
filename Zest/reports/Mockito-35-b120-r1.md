# Zest – Mockito-35 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 229345 (valid 97.51%) |
| Cycles | 24 |
| Corpus | 46 |
| Zest branch coverage (total / valid) | 217 / 214 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 46 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 19 / 51 = 37.25% |
| Branch coverage | 0 / 0 = 0% |
| Test suite length (statements) | 46 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 120s |
| Pipeline time | 260s |

## Failing tests on buggy version

- org.mockito.Matchers_Zest_Test::test18
- org.mockito.Matchers_Zest_Test::test20
- org.mockito.Matchers_Zest_Test::test25
- org.mockito.Matchers_Zest_Test::test34
- org.mockito.Matchers_Zest_Test::test40
- org.mockito.Matchers_Zest_Test::test43
- org.mockito.Matchers_Zest_Test::test4

```
--- org.mockito.Matchers_Zest_Test::test18
junit.framework.AssertionFailedError: expected:<0=null
1=[!java.lang.NullPointerException]
> but was:<0=null
1=[null]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.Matchers_Zest_Test.test18(Matchers_Zest_Test.java:133)
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
--- org.mockito.Matchers_Zest_Test::test20
junit.framework.AssertionFailedError: expected:<0=null
1=[!java.lang.NullPointerException]
> but was:<0=null
1=[null]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.Matchers_Zest_Test.test20(Matchers_Zest_Test.java:147)
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

- Suite: `/home/user/suites/Mockito/zest/35/t12001/Mockito-35f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_35/b120_r1/src/org/mockito/Matchers_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_35/b120_r1`
