# Zest – Jsoup-18 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 395634 (valid 91.31%) |
| Cycles | 24 |
| Corpus | 92 |
| Zest branch coverage (total / valid) | 211 / 204 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 92 |
| Failing on fixed | 0 |
| Failing on buggy | 14 |
| Bug detection | Fail (triggering: 14) |
| **Fault detected** | **yes** |
| Line coverage | 72 / 98 = 73.47% |
| Branch coverage | 54 / 82 = 65.85% |
| Test suite length (statements) | 92 |
| Mutation score | 158 / 180 = 87.78% |
| Fuzz time | 120s |
| Pipeline time | 211s |

## Failing tests on buggy version


```
--- org.jsoup.parser.CharacterReader_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<....lang.String:򊪮𤕌񚰈?[? |this=org.jsoup.parser.CharacterReader:
1=java.lang.String: |this=org.jsoup.parser.CharacterReader:]
> but was:<....lang.String:򊪮𤕌񚰈?[ |this=org.jsoup.parser.CharacterReader:
1=!java.lang.StringIndexOutOfBoundsException]
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
--- org.jsoup.parser.CharacterReader_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<....lang.String:񛤵𰑤񚰈?[? |this=org.jsoup.parser.CharacterReader:
1=java.lang.String: |this=org.jsoup.parser.CharacterReader:]
> but was:<....lang.String:񛤵𰑤񚰈?[ |this=org.jsoup.parser.CharacterReader:
1=!java.lang.StringIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.CharacterReader_Zest_Test.test17(CharacterReader_Zest_Test.java:126)
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

- Suite: `/home/user/suites/Jsoup/zest/18/t12001/Jsoup-18f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_18/b120_r1/src/org/jsoup/parser/CharacterReader_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_18/b120_r1`
