# Zest – Jsoup-50 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 415187 (valid 60.07%) |
| Cycles | 40 |
| Corpus | 23 |
| Zest branch coverage (total / valid) | 149 / 146 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 25 / 95 = 26.32% |
| Branch coverage | 7 / 76 = 9.21% |
| Test suite length (statements) | 21 |
| Mutation score | 254 / 264 = 96.21% |
| Fuzz time | 120s |
| Pipeline time | 184s |

## Failing tests on buggy version

- org.jsoup.helper.DataUtil_Zest_Test::test3

```
--- org.jsoup.helper.DataUtil_Zest_Test::test3
junit.framework.AssertionFailedError: expected:<0=!java.[lang.NullPointer]Exception
> but was:<0=!java.[nio.charset.IllegalCharsetName]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.helper.DataUtil_Zest_Test.test3(DataUtil_Zest_Test.java:28)
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

- Suite: `/home/user/suites/Jsoup/zest/50/t12001/Jsoup-50f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_50/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_50/b120_r1`
