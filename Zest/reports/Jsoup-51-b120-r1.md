# Zest – Jsoup-51 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 379520 (valid 89.66%) |
| Cycles | 19 |
| Corpus | 188 |
| Zest branch coverage (total / valid) | 304 / 297 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 188 |
| Failing on fixed | 0 |
| Failing on buggy | 2 |
| Bug detection | Fail (triggering: 2) |
| **Fault detected** | **yes** |
| Line coverage | 145 / 195 = 74.36% |
| Branch coverage | 141 / 188 = 75.00% |
| Test suite length (statements) | 188 |
| Mutation score | 428 / 543 = 78.82% |
| Fuzz time | 120s |
| Pipeline time | 230s |

## Failing tests on buggy version

- org.jsoup.parser.CharacterReader_Zest_Test::test50
- org.jsoup.parser.CharacterReader_Zest_Test::test68

```
--- org.jsoup.parser.CharacterReader_Zest_Test::test50
junit.framework.AssertionFailedError: expected:<0=java.lang.Boolean:[true |this=org.jsoup.parser.CharacterReader:ൡȀ
1=java.lang.Boolean:tru]e |this=org.jsoup.pa...> but was:<0=java.lang.Boolean:[false |this=org.jsoup.parser.CharacterReader:ൡȀ
1=java.lang.Boolean:fals]e |this=org.jsoup.pa...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test50(CharacterReader_Zest_Test.java:357)
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
--- org.jsoup.parser.CharacterReader_Zest_Test::test68
junit.framework.AssertionFailedError: expected:<...1=java.lang.Boolean:[tru]e |this=org.jsoup.pa...> but was:<...1=java.lang.Boolean:[fals]e |this=org.jsoup.pa...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test68(CharacterReader_Zest_Test.java:483)
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

- Suite: `/home/user/suites/Jsoup/zest/51/t12001/Jsoup-51f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_51/b120_r1/src/org/jsoup/parser/CharacterReader_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_51/b120_r1`
