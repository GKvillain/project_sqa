# Zest – Jsoup-91 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 360955 (valid 90.25%) |
| Cycles | 21 |
| Corpus | 93 |
| Zest branch coverage (total / valid) | 257 / 254 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 93 |
| Failing on fixed | 0 |
| Failing on buggy | 39 |
| Bug detection | Fail (triggering: 39) |
| **Fault detected** | **yes** |
| Line coverage | 113 / 258 = 43.80% |
| Branch coverage | 83 / 198 = 41.92% |
| Test suite length (statements) | 100 |
| Mutation score | 482 / 580 = 83.10% |
| Fuzz time | 120s |
| Pipeline time | 202s |

## Failing tests on buggy version


```
--- org.jsoup.UncheckedIOException_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[new org.jsoup.UncheckedIOException:org.jsoup.UncheckedIOException: java.io.IOException: 򊷑
1=!java.lang.IllegalArgument]Exception
> but was:<0=[!H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.UncheckedIOException_Zest_Test.test10(UncheckedIOException_Zest_Test.java:77)
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
--- org.jsoup.UncheckedIOException_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...CharacterReader:?
1=[java.lang.Boolean:false |this=org.jsoup.parser.CharacterReader:?]
> but was:<...CharacterReader:?
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.UncheckedIOException_Zest_Test.test11(UncheckedIOException_Zest_Test.java:84)
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

- Suite: `/home/user/suites/Jsoup/zest/91/t12001/Jsoup-91f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_91/b120_r1/src/org/jsoup/UncheckedIOException_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_91/b120_r1`
