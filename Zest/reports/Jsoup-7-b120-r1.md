# Zest – Jsoup-7 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 265667 (valid 81.22%) |
| Cycles | 21 |
| Corpus | 56 |
| Zest branch coverage (total / valid) | 406 / 399 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 56 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 66 / 96 = 68.75% |
| Branch coverage | 20 / 36 = 55.56% |
| Test suite length (statements) | 454 |
| Mutation score | 55 / 71 = 77.46% |
| Fuzz time | 120s |
| Pipeline time | 178s |

## Failing tests on buggy version

- org.jsoup.nodes.Document_Zest_Test::test12

```
--- org.jsoup.nodes.Document_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Document_Zest_Test.test12(Document_Zest_Test.java:91)
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

- Suite: `/home/user/suites/Jsoup/zest/7/t12001/Jsoup-7f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_7/b120_r1/src/org/jsoup/nodes/Document_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_7/b120_r1`
