# Zest – Jsoup-52 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 397409 (valid 51.27%) |
| Cycles | 38 |
| Corpus | 32 |
| Zest branch coverage (total / valid) | 223 / 220 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 26 |
| Failing on fixed | 0 |
| Failing on buggy | 6 |
| Bug detection | Fail (triggering: 6) |
| **Fault detected** | **yes** |
| Line coverage | 47 / 186 = 25.27% |
| Branch coverage | 9 / 115 = 7.83% |
| Test suite length (statements) | 26 |
| Mutation score | 346 / 362 = 95.58% |
| Fuzz time | 120s |
| Pipeline time | 182s |

## Failing tests on buggy version


```
--- org.jsoup.helper.DataUtil_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[ |this=org.jsoup.nodes.XmlDeclaration:<?񰓇𺺺?]>
1=java.nio.HeapByt...> but was:<0=java.lang.String:[񰓇𺺺 |this=org.jsoup.nodes.XmlDeclaration:<?񰓇𺺺]>
1=java.nio.HeapByt...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.helper.DataUtil_Zest_Test.test11(DataUtil_Zest_Test.java:84)
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
--- org.jsoup.helper.DataUtil_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[ |this=org.jsoup.nodes.XmlDeclaration:<!󘭬𣣍!]>
1=!java.lang.NullP...> but was:<0=java.lang.String:[󘭬𣣍 |this=org.jsoup.nodes.XmlDeclaration:<!󘭬𣣍]>
1=!java.lang.NullP...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.helper.DataUtil_Zest_Test.test12(DataUtil_Zest_Test.java:91)
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
--- org.jsoup.helper.DataUtil_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=java.lang.String:[ |this=org.jsoup.nodes.XmlDeclaration:<!𣣍!]>
```

- Suite: `/home/user/suites/Jsoup/zest/52/t12001/Jsoup-52f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_52/b120_r1/src/org/jsoup/helper/DataUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_52/b120_r1`
