# Zest – Jsoup-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 197552 (valid 79.88%) |
| Cycles | 9 |
| Corpus | 144 |
| Zest branch coverage (total / valid) | 645 / 638 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 144 |
| Failing on fixed | 0 |
| Failing on buggy | 12 |
| Bug detection | Fail (triggering: 12) |
| **Fault detected** | **yes** |
| Line coverage | 130 / 173 = 75.14% |
| Branch coverage | 62 / 94 = 65.96% |
| Test suite length (statements) | 3147 |
| Mutation score | 138 / 199 = 69.35% |
| Fuzz time | 120s |
| Pipeline time | 246s |

## Failing tests on buggy version


```
--- org.jsoup.parser.Parser_Zest_Test::test114
junit.framework.AssertionFailedError: expected:<0=[null |this=org.jsoup.parser.Parser:@?]
> but was:<0=[!java.lang.StringIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.Parser_Zest_Test.test114(Parser_Zest_Test.java:805)
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
--- org.jsoup.parser.Parser_Zest_Test::test116
junit.framework.AssertionFailedError: expected:<0=[org.jsoup.nodes.Document:<html>
 <head></head>
 <body>
  &#56277;&#56748;&#56212;&#56833;&#56109;&#56847;&#56112;&#56906;&#55479;&#56326;&#56294;&#57287;&#55885;&#56511;&#55444;&#56738;&#56216;&#56827;&#55737;&#57066;&#56317;&#56320;&#56223;&#57049;&#56051;&#56922;&#55682;&#57162;&#55630;&#56780;&#55811;&#57297;<瀎></瀎>
 </...]
> but was:<0=[!java.lang.StringIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.parser.Parser_Zest_Test.test116(Parser_Zest_Test.java:819)
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

- Suite: `/home/user/suites/Jsoup/zest/5/t12001/Jsoup-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_5/b120_r1/src/org/jsoup/parser/Parser_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_5/b120_r1`
