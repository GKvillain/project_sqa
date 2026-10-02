# Zest – Jsoup-28 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 243807 (valid 64.65%) |
| Cycles | 43 |
| Corpus | 56 |
| Zest branch coverage (total / valid) | 526 / 283 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 56 |
| Failing on fixed | 0 |
| Failing on buggy | 11 |
| Bug detection | Fail (triggering: 11) |
| **Fault detected** | **yes** |
| Line coverage | 85 / 223 = 38.12% |
| Branch coverage | 15 / 98 = 15.31% |
| Test suite length (statements) | 57 |
| Mutation score | 0 / 0 = NA% |
| Fuzz time | 120s |
| Pipeline time | 196s |

## Failing tests on buggy version

- org.jsoup.nodes.Entities_Zest_Test::test14
- org.jsoup.nodes.Entities_Zest_Test::test29
- org.jsoup.nodes.Entities_Zest_Test::test30
- org.jsoup.nodes.Entities_Zest_Test::test31
- org.jsoup.nodes.Entities_Zest_Test::test32
- org.jsoup.nodes.Entities_Zest_Test::test33
- org.jsoup.nodes.Entities_Zest_Test::test35
- org.jsoup.nodes.Entities_Zest_Test::test39
- org.jsoup.nodes.Entities_Zest_Test::test45
- org.jsoup.nodes.Entities_Zest_Test::test3
- org.jsoup.nodes.Entities_Zest_Test::test4

```
--- org.jsoup.nodes.Entities_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<0=![java.lang.IllegalArgument]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Entities_Zest_Test.test14(Entities_Zest_Test.java:105)
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
--- org.jsoup.nodes.Entities_Zest_Test::test29
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:⺦񟒪񝧶򟾤󒻡󴊒𛰩񊶰򂊀񴉟񮝑򸴅󢐉􅜓򏊌󞍗򆮏򩔛򳚱񩒼􉽦𲷘ﳂ񜮬򎙑󞯗𡒝񹹌򜡹󏥑񪐗񒚌񐆞򗆁򃙌񲣙]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Entities_Zest_Test.test29(Entities_Zest_Test.java:210)
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
--- org.jsoup.nodes.Entities_Zest_Test::test30
junit.framework.AssertionFailedError: expected:<...va.lang.String:򣄹
```

- Suite: `/home/user/suites/Jsoup/zest/28/t12001/Jsoup-28f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_28/b120_r1/src/org/jsoup/nodes/Entities_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_28/b120_r1`
