# Zest – Mockito-25 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 279815 (valid 45.03%) |
| Cycles | 35 |
| Corpus | 21 |
| Zest branch coverage (total / valid) | 182 / 173 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 13 |
| Bug detection | Fail (triggering: 13) |
| **Fault detected** | **yes** |
| Line coverage | 12 / 32 = 37.50% |
| Branch coverage | 0 / 8 = 0.00% |
| Test suite length (statements) | 21 |
| Mutation score | 10 / 11 = 90.91% |
| Fuzz time | 120s |
| Pipeline time | 400s |

## Failing tests on buggy version

- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test10
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test13
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test14
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test16
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test19
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test20
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test0
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test1
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test2
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test3
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test5
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test6
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test8

```
--- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs$1:@? |this=org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs:@?
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test.test10(ReturnsDeepStubs_Zest_Test.java:77)
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
--- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=[org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs$1:@? |this=org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs:@?
1=org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs$1:@? |this=org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test.test13(ReturnsDeepStubs_Zest_Test.java:98)
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

- Suite: `/home/user/suites/Mockito/zest/25/t12001/Mockito-25f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_25/b120_r1/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_25/b120_r1`
