# Zest – Jsoup-23 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 421288 (valid 84.76%) |
| Cycles | 25 |
| Corpus | 96 |
| Zest branch coverage (total / valid) | 227 / 219 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 96 |
| Failing on fixed | 0 |
| Failing on buggy | 18 |
| Bug detection | Fail (triggering: 18) |
| **Fault detected** | **yes** |
| Line coverage | 88 / 229 = 38.43% |
| Branch coverage | 68 / 168 = 40.48% |
| Test suite length (statements) | 96 |
| Mutation score | 355 / 387 = 91.73% |
| Fuzz time | 120s |
| Pipeline time | 224s |

## Failing tests on buggy version


```
--- org.jsoup.parser.CharacterReader_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[java.lang.String: |this=org.jsoup.parser.CharacterReader:򵧪𰪗𮣁󸍋񌆗]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test10(CharacterReader_Zest_Test.java:77)
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
--- org.jsoup.parser.CharacterReader_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[java.lang.String: |this=org.jsoup.parser.CharacterReader:􅄤𦯹󱝹􂞗񌆗꯾󁾣󿹄󗠽󀧐򡴺􄱻
1=java.lang.String: |a0=[C:[[?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ꯾, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?]] |this=org.jsoup.parser.CharacterReader:􅄤𦯹󱝹􂞗񌆗꯾󁾣󿹄󗠽󀧐򡴺􄱻]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test15(CharacterReader_Zest_Test.java:112)
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
--- org.jsoup.parser.CharacterReader_Zest_Test::test18
```

- Suite: `/home/user/suites/Jsoup/zest/23/t12001/Jsoup-23f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_23/b120_r1/src/org/jsoup/parser/CharacterReader_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_23/b120_r1`
