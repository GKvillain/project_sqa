# Zest – Jsoup-71 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 402034 (valid 47.24%) |
| Cycles | 23 |
| Corpus | 126 |
| Zest branch coverage (total / valid) | 425 / 415 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 126 |
| Failing on fixed | 0 |
| Failing on buggy | 5 |
| Bug detection | Fail (triggering: 5) |
| **Fault detected** | **yes** |
| Line coverage | 152 / 448 = 33.93% |
| Branch coverage | 74 / 254 = 29.13% |
| Test suite length (statements) | 127 |
| Mutation score | 434 / 534 = 81.27% |
| Fuzz time | 120s |
| Pipeline time | 243s |

## Failing tests on buggy version

- org.jsoup.nodes.PseudoTextElement_Zest_Test::test12
- org.jsoup.nodes.PseudoTextElement_Zest_Test::test41
- org.jsoup.nodes.PseudoTextElement_Zest_Test::test42
- org.jsoup.nodes.PseudoTextElement_Zest_Test::test57
- org.jsoup.nodes.PseudoTextElement_Zest_Test::test7

```
--- org.jsoup.nodes.PseudoTextElement_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=![java.lang.IllegalArgument]Exception
> but was:<0=![H:java.lang.ClassNotFound]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.PseudoTextElement_Zest_Test.test12(PseudoTextElement_Zest_Test.java:91)
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
--- org.jsoup.nodes.PseudoTextElement_Zest_Test::test41
junit.framework.AssertionFailedError: expected:<...t.QueryParser:@?
1=![java.lang.IllegalArgument]Exception
> but was:<...t.QueryParser:@?
1=![H:java.lang.ClassNotFound]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.PseudoTextElement_Zest_Test.test41(PseudoTextElement_Zest_Test.java:294)
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

- Suite: `/home/user/suites/Jsoup/zest/71/t12001/Jsoup-71f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_71/b120_r1/src/org/jsoup/nodes/PseudoTextElement_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_71/b120_r1`
