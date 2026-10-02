# Zest – Jsoup-58 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 267799 (valid 78.22%) |
| Cycles | 4 |
| Corpus | 317 |
| Zest branch coverage (total / valid) | 1121 / 1118 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 317 |
| Failing on fixed | 0 |
| Failing on buggy | 26 |
| Bug detection | Fail (triggering: 26) |
| **Fault detected** | **yes** |
| Line coverage | 38 / 137 = 27.74% |
| Branch coverage | 4 / 38 = 10.53% |
| Test suite length (statements) | 419 |
| Mutation score | 88 / 97 = 90.72% |
| Fuzz time | 120s |
| Pipeline time | 204s |

## Failing tests on buggy version

- org.jsoup.Jsoup_Zest_Test::test181
- org.jsoup.Jsoup_Zest_Test::test276
- org.jsoup.Jsoup_Zest_Test::test312
- org.jsoup.Jsoup_Zest_Test::test12
- org.jsoup.Jsoup_Zest_Test::test16
- org.jsoup.Jsoup_Zest_Test::test24
- org.jsoup.Jsoup_Zest_Test::test25
- org.jsoup.Jsoup_Zest_Test::test26
- org.jsoup.Jsoup_Zest_Test::test29
- org.jsoup.Jsoup_Zest_Test::test36
- org.jsoup.Jsoup_Zest_Test::test37
- org.jsoup.Jsoup_Zest_Test::test38
- org.jsoup.Jsoup_Zest_Test::test39
- org.jsoup.Jsoup_Zest_Test::test40
- org.jsoup.Jsoup_Zest_Test::test42
- org.jsoup.Jsoup_Zest_Test::test49
- org.jsoup.Jsoup_Zest_Test::test50
- org.jsoup.Jsoup_Zest_Test::test51
- org.jsoup.Jsoup_Zest_Test::test52
- org.jsoup.Jsoup_Zest_Test::test53
- org.jsoup.Jsoup_Zest_Test::test57
- org.jsoup.Jsoup_Zest_Test::test63
- org.jsoup.Jsoup_Zest_Test::test68
- org.jsoup.Jsoup_Zest_Test::test69
- org.jsoup.Jsoup_Zest_Test::test4
- org.jsoup.Jsoup_Zest_Test::test6

```
--- org.jsoup.Jsoup_Zest_Test::test181
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.Jsoup_Zest_Test.test181(Jsoup_Zest_Test.java:1274)
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
--- org.jsoup.Jsoup_Zest_Test::test276
junit.framework.AssertionFailedError: expected:<0=[java.util.Collections$UnmodifiableRandomAccessList:[<html>
 <head></head>
 <body>
  񵱙󝘗򍽖𷂅𦳶񑂝񎧇򩑝󅾐󋠢򕹁񰗯&amp;ÍtE􉨒󭶊򐃖󉞚󌹌 븀
 </body>
</html>]
1=java.util.Collections$UnmodifiableRandomAccessList:[<html>
 <head></head>
 <body>
  񵱙󝘗򍽖𷂅𦳶񑂝񎧇򩑝󅾐󋠢򕹁񰗯&amp;ÍtE􉨒󭶊򐃖󉞚󌹌 븀
 </body>
</html>]]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.Jsoup_Zest_Test.test276(Jsoup_Zest_Test.java:1939)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
```

- Suite: `/home/user/suites/Jsoup/zest/58/t12001/Jsoup-58f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_58/b120_r1/src/org/jsoup/Jsoup_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_58/b120_r1`
