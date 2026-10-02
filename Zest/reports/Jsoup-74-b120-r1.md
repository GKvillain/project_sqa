# Zest – Jsoup-74 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 380273 (valid 95.09%) |
| Cycles | 16 |
| Corpus | 112 |
| Zest branch coverage (total / valid) | 238 / 236 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 112 |
| Failing on fixed | 0 |
| Failing on buggy | 18 |
| Bug detection | Fail (triggering: 18) |
| **Fault detected** | **yes** |
| Line coverage | 75 / 85 = 88.24% |
| Branch coverage | 78 / 86 = 90.70% |
| Test suite length (statements) | 153 |
| Mutation score | 189 / 229 = 82.53% |
| Fuzz time | 120s |
| Pipeline time | 195s |

## Failing tests on buggy version

- org.jsoup.helper.StringUtil_Zest_Test::test100
- org.jsoup.helper.StringUtil_Zest_Test::test101
- org.jsoup.helper.StringUtil_Zest_Test::test102
- org.jsoup.helper.StringUtil_Zest_Test::test10
- org.jsoup.helper.StringUtil_Zest_Test::test12
- org.jsoup.helper.StringUtil_Zest_Test::test15
- org.jsoup.helper.StringUtil_Zest_Test::test18
- org.jsoup.helper.StringUtil_Zest_Test::test25
- org.jsoup.helper.StringUtil_Zest_Test::test26
- org.jsoup.helper.StringUtil_Zest_Test::test28
- org.jsoup.helper.StringUtil_Zest_Test::test85
- org.jsoup.helper.StringUtil_Zest_Test::test94
- org.jsoup.helper.StringUtil_Zest_Test::test95
- org.jsoup.helper.StringUtil_Zest_Test::test96
- org.jsoup.helper.StringUtil_Zest_Test::test98
- org.jsoup.helper.StringUtil_Zest_Test::test0
- org.jsoup.helper.StringUtil_Zest_Test::test2
- org.jsoup.helper.StringUtil_Zest_Test::test6

```
--- org.jsoup.helper.StringUtil_Zest_Test::test100
junit.framework.AssertionFailedError: expected:<...?􌜺ǐ㑮痈膣ᒍ؜罬ᖼ㒉㥜灅拝䰠풫鰓[]
1=java.lang.Boolean...> but was:<...?􌜺ǐ㑮痈膣ᒍ؜罬ᖼ㒉㥜灅拝䰠풫鰓[‌]
1=java.lang.Boolean...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.helper.StringUtil_Zest_Test.test100(StringUtil_Zest_Test.java:707)
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
--- org.jsoup.helper.StringUtil_Zest_Test::test101
junit.framework.AssertionFailedError: expected:<...?􌜺ǐ㑮痈뵯띝؜罬ᖼ㒉迠퍈ໆ䰠풫儜[
1=void |a0=java.lang.StringBuilder:򛗝𕋼򓁜񚋈􌜺ǐ㑮痈뵯띝؜罬ᖼ㒉迠퍈ໆ䰠풫儜]
> but was:<...?􌜺ǐ㑮痈뵯띝؜罬ᖼ㒉迠퍈ໆ䰠풫儜[‌
1=void |a0=java.lang.StringBuilder:򛗝𕋼򓁜񚋈􌜺ǐ㑮痈뵯띝؜罬ᖼ㒉迠퍈ໆ䰠풫儜‌]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.helper.StringUtil_Zest_Test.test101(StringUtil_Zest_Test.java:714)
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

- Suite: `/home/user/suites/Jsoup/zest/74/t12001/Jsoup-74f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_74/b120_r1/src/org/jsoup/helper/StringUtil_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_74/b120_r1`
