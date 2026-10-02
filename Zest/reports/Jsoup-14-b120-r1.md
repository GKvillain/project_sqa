# Zest – Jsoup-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 391868 (valid 71.75%) |
| Cycles | 27 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 176 / 172 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 173 / 1319 = 13.12% |
| Branch coverage | 5 / 434 = 1.15% |
| Test suite length (statements) | 40 |
| Mutation score | 952 / 968 = 98.35% |
| Fuzz time | 120s |
| Pipeline time | 185s |

## Failing tests on buggy version

- org.jsoup.parser.Tokeniser_Zest_Test::test11
- org.jsoup.parser.Tokeniser_Zest_Test::test14
- org.jsoup.parser.Tokeniser_Zest_Test::test39
- org.jsoup.parser.Tokeniser_Zest_Test::test0
- org.jsoup.parser.Tokeniser_Zest_Test::test1
- org.jsoup.parser.Tokeniser_Zest_Test::test2

```
--- org.jsoup.parser.Tokeniser_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...ser.Tokeniser:@?
1=![java.lang.NullPointer]Exception
> but was:<...ser.Tokeniser:@?
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.Tokeniser_Zest_Test.test11(Tokeniser_Zest_Test.java:84)
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
--- org.jsoup.parser.Tokeniser_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<...ser.Tokeniser:@?
1=![java.lang.NullPointer]Exception
> but was:<...ser.Tokeniser:@?
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.Tokeniser_Zest_Test.test14(Tokeniser_Zest_Test.java:105)
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

- Suite: `/home/user/suites/Jsoup/zest/14/t12001/Jsoup-14f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_14/b120_r1/src/org/jsoup/parser/Tokeniser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_14/b120_r1`
