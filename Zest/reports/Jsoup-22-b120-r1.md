# Zest – Jsoup-22 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 358561 (valid 68.81%) |
| Cycles | 46 |
| Corpus | 34 |
| Zest branch coverage (total / valid) | 239 / 236 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 34 |
| Failing on fixed | 0 |
| Failing on buggy | 12 |
| Bug detection | Fail (triggering: 12) |
| **Fault detected** | **yes** |
| Line coverage | 63 / 688 = 9.16% |
| Branch coverage | 10 / 290 = 3.45% |
| Test suite length (statements) | 42 |
| Mutation score | 560 / 574 = 97.56% |
| Fuzz time | 120s |
| Pipeline time | 197s |

## Failing tests on buggy version

- org.jsoup.nodes.Element_Zest_Test::test11
- org.jsoup.nodes.Element_Zest_Test::test15
- org.jsoup.nodes.Element_Zest_Test::test16
- org.jsoup.nodes.Element_Zest_Test::test18
- org.jsoup.nodes.Element_Zest_Test::test19
- org.jsoup.nodes.Element_Zest_Test::test20
- org.jsoup.nodes.Element_Zest_Test::test21
- org.jsoup.nodes.Element_Zest_Test::test24
- org.jsoup.nodes.Element_Zest_Test::test27
- org.jsoup.nodes.Element_Zest_Test::test30
- org.jsoup.nodes.Element_Zest_Test::test6
- org.jsoup.nodes.Element_Zest_Test::test9

```
--- org.jsoup.nodes.Element_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[java.util.Collections$EmptyList:[] |this=org.jsoup.nodes.Comment:
<!--혗򳖼󒆬𷥻񫛙򑛫𔆻򁮹򄏭隋𣌻𴪮-->]
> but was:<0=[!java.lang.NullPointerException]
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
junit.framework.AssertionFailedError: expected:<0=[null |this=org.jsoup.nodes.Comment:
<!--񆄣񄙻𵄭󠮚񫛙򑛫𔆻򁮹򄏭隋𦝑񍓲-->
1=new org.jsoup.select.Elements:!toString:java.lang.NullPointerException |a0=[Lorg.jsoup.nodes.Element;:[[null, null, null]]]
> but was:<0=[!java.lang.NullPointerException]
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
```

- Suite: `/home/user/suites/Jsoup/zest/22/t12001/Jsoup-22f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_22/b120_r1/src/org/jsoup/nodes/Element_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_22/b120_r1`
