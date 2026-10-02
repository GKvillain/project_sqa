# Zest – Jsoup-87 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 417688 (valid 57.92%) |
| Cycles | 38 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 261 / 257 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 21 |
| Bug detection | Fail (triggering: 21) |
| **Fault detected** | **yes** |
| Line coverage | 174 / 2009 = 8.66% |
| Branch coverage | 22 / 1259 = 1.75% |
| Test suite length (statements) | 44 |
| Mutation score | 2517 / 2612 = 96.36% |
| Fuzz time | 121s |
| Pipeline time | 193s |

## Failing tests on buggy version

- org.jsoup.nodes.Element_Zest_Test::test11
- org.jsoup.nodes.Element_Zest_Test::test15
- org.jsoup.nodes.Element_Zest_Test::test18
- org.jsoup.nodes.Element_Zest_Test::test19
- org.jsoup.nodes.Element_Zest_Test::test20
- org.jsoup.nodes.Element_Zest_Test::test21
- org.jsoup.nodes.Element_Zest_Test::test24
- org.jsoup.nodes.Element_Zest_Test::test26
- org.jsoup.nodes.Element_Zest_Test::test28
- org.jsoup.nodes.Element_Zest_Test::test29
- org.jsoup.nodes.Element_Zest_Test::test31
- org.jsoup.nodes.Element_Zest_Test::test32
- org.jsoup.nodes.Element_Zest_Test::test35
- org.jsoup.nodes.Element_Zest_Test::test36
- org.jsoup.nodes.Element_Zest_Test::test37
- org.jsoup.nodes.Element_Zest_Test::test38
- org.jsoup.nodes.Element_Zest_Test::test40
- org.jsoup.nodes.Element_Zest_Test::test41
- org.jsoup.nodes.Element_Zest_Test::test42
- org.jsoup.nodes.Element_Zest_Test::test8
- org.jsoup.nodes.Element_Zest_Test::test9

```
--- org.jsoup.nodes.Element_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=![java.lang.IllegalArgument]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Element_Zest_Test.test11(Element_Zest_Test.java:84)
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
--- org.jsoup.nodes.Element_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:򹰄񆆻 |this=org.jsoup.nodes.Element:<򹰄񆆻></򹰄񆆻>]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Element_Zest_Test.test15(Element_Zest_Test.java:112)
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
--- org.jsoup.nodes.Element_Zest_Test::test18
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:􏨰񆆻󆖆򶡣񲫎񖫒񥤶镘󎐜꨽􍟙 |this=org.jsoup.parser.Tag:􏨰񆆻󆖆򶡣񲫎񖫒񥤶镘󎐜꨽􍟙]
```

- Suite: `/home/user/suites/Jsoup/zest/87/t12001/Jsoup-87f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_87/b120_r1/src/org/jsoup/nodes/Element_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_87/b120_r1`
