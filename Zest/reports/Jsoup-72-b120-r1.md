# Zest – Jsoup-72 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 359509 (valid 88.96%) |
| Cycles | 17 |
| Corpus | 210 |
| Zest branch coverage (total / valid) | 358 / 355 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 210 |
| Failing on fixed | 0 |
| Failing on buggy | 28 |
| Bug detection | Fail (triggering: 28) |
| **Fault detected** | **yes** |
| Line coverage | 163 / 230 = 70.87% |
| Branch coverage | 140 / 200 = 70.00% |
| Test suite length (statements) | 215 |
| Mutation score | 472 / 593 = 79.60% |
| Fuzz time | 120s |
| Pipeline time | 244s |

## Failing tests on buggy version


```
--- org.jsoup.parser.CharacterReader_Zest_Test::test106
junit.framework.AssertionFailedError: expected:<....CharacterReader:
1=[java.lang.String: |this=org.jsoup.parser.CharacterReader:!toString:]java.lang.StringInde...> but was:<....CharacterReader:
1=[!]java.lang.StringInde...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test106(CharacterReader_Zest_Test.java:749)
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
--- org.jsoup.parser.CharacterReader_Zest_Test::test109
junit.framework.AssertionFailedError: expected:<....CharacterReader:
1=[java.lang.String: |this=org.jsoup.parser.CharacterReader:!toString:]java.lang.StringInde...> but was:<....CharacterReader:
1=[!]java.lang.StringInde...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test109(CharacterReader_Zest_Test.java:770)
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
--- org.jsoup.parser.CharacterReader_Zest_Test::test146
junit.framework.AssertionFailedError: expected:<....CharacterReader:
```

- Suite: `/home/user/suites/Jsoup/zest/72/t12001/Jsoup-72f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_72/b120_r1/src/org/jsoup/parser/CharacterReader_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_72/b120_r1`
