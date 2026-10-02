# Zest – Mockito-28 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 283885 (valid 65.18%) |
| Cycles | 37 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 166 / 163 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 11 |
| Bug detection | Fail (triggering: 11) |
| **Fault detected** | **yes** |
| Line coverage | 18 / 31 = 58.06% |
| Branch coverage | 6 / 10 = 60.00% |
| Test suite length (statements) | 37 |
| Mutation score | 12 / 17 = 70.59% |
| Fuzz time | 120s |
| Pipeline time | 292s |

## Failing tests on buggy version

- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test14
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test19
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test20
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test24
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test25
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test0
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test1
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test2
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test3
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test4
- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test7

```
--- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<...njectionEngine:@?
1=[!java.lang.NullPointerException]
> but was:<...njectionEngine:@?
1=[void |this=org.mockito.internal.configuration.DefaultInjectionEngine:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test.test14(DefaultInjectionEngine_Zest_Test.java:105)
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
--- org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test::test19
junit.framework.AssertionFailedError: expected:<...njectionEngine:@?
1=[!java.lang.NullPointerException]
> but was:<...njectionEngine:@?
1=[void |this=org.mockito.internal.configuration.DefaultInjectionEngine:@?]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.configuration.DefaultInjectionEngine_Zest_Test.test19(DefaultInjectionEngine_Zest_Test.java:140)
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

- Suite: `/home/user/suites/Mockito/zest/28/t12001/Mockito-28f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_28/b120_r1/src/org/mockito/internal/configuration/DefaultInjectionEngine_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_28/b120_r1`
