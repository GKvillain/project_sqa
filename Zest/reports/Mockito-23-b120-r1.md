# Zest – Mockito-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 305838 (valid 63.26%) |
| Cycles | 36 |
| Corpus | 26 |
| Zest branch coverage (total / valid) | 193 / 184 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 12 |
| Bug detection | Fail (triggering: 12) |
| **Fault detected** | **yes** |
| Line coverage | 16 / 41 = 39.02% |
| Branch coverage | 4 / 12 = 33.33% |
| Test suite length (statements) | 26 |
| Mutation score | 12 / 17 = 70.59% |
| Fuzz time | 120s |
| Pipeline time | 460s |

## Failing tests on buggy version

- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test10
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test12
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test13
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test15
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test17
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test23
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test25
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test0
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test1
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test2
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test5
- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test6

```
--- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...turnsDeepStubs:@?
1=[void |this=org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs:@?]
> but was:<...turnsDeepStubs:@?
1=[!H:java.lang.NoSuchMethodException]
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
--- org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[void |this=org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs:@?
1=!java.lang.NullPointer]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs_Zest_Test.test12(ReturnsDeepStubs_Zest_Test.java:91)
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

- Suite: `/home/user/suites/Mockito/zest/23/t12001/Mockito-23f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Mockito_23/b120_r1/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Mockito_23/b120_r1`
