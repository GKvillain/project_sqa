# Zest – Jsoup-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 693676 (valid 0.00%) |
| Cycles | 109 |
| Corpus | 11 |
| Zest branch coverage (total / valid) | 129 / 0 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 10 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 15 / 218 = 6.88% |
| Branch coverage | 3 / 132 = 2.27% |
| Test suite length (statements) | 10 |
| Mutation score | 148 / 153 = 96.73% |
| Fuzz time | 120s |
| Pipeline time | 179s |

## Failing tests on buggy version

- org.jsoup.select.Selector_Zest_Test::test4

```
--- org.jsoup.select.Selector_Zest_Test::test4
junit.framework.AssertionFailedError: expected:<0=![java.lang.ClassCast]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.select.Selector_Zest_Test.test4(Selector_Zest_Test.java:35)
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

- Suite: `/home/user/suites/Jsoup/zest/11/t12001/Jsoup-11f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_11/b120_r1/src/org/jsoup/select/Selector_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_11/b120_r1`
