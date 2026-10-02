# Zest – Mockito-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 346026 (valid 70.46%) |
| Cycles | 31 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 249 / 246 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 21 |
| Bug detection | Fail (triggering: 21) |
| **Fault detected** | **yes** |
| Line coverage | 49 / 85 = 57.65% |
| Branch coverage | 17 / 36 = 47.22% |
| Test suite length (statements) | 40 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 123s |
| Pipeline time | 734s |

## Failing tests on buggy version

- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test11
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test13
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test16
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test17
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test20
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test21
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test24
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test26
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test30
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test31
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test36
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test37
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test38
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test39
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test0
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test1
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test2
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test3
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test5
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test7
- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test9

```
--- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter$1:@? |this=org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter:@?
1=org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter$1:@? |this=org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test.test11(FinalMockCandidateFilter_Zest_Test.java:84)
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
--- org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<...tterInjection:@?
1=![org.mockito.exceptions.misusing.NotAMock]Exception
> but was:<...tterInjection:@?
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter_Zest_Test.test13(FinalMockCandidateFilter_Zest_Test.java:98)
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
```

- Suite: `/home/user/suites/Mockito/zest/19/t12001/Mockito-19f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_19/b120_r1/src/org/mockito/internal/configuration/injection/filter/FinalMockCandidateFilter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_19/b120_r1`
