# Zest – Jsoup-92 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 346976 (valid 77.32%) |
| Cycles | 20 |
| Corpus | 68 |
| Zest branch coverage (total / valid) | 274 / 271 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 68 |
| Failing on fixed | 0 |
| Failing on buggy | 49 |
| Bug detection | Fail (triggering: 49) |
| **Fault detected** | **yes** |
| Line coverage | 102 / 819 = 12.45% |
| Branch coverage | 24 / 385 = 6.23% |
| Test suite length (statements) | 89 |
| Mutation score | 965 / 1004 = 96.12% |
| Fuzz time | 119s |
| Pipeline time | 184s |

## Failing tests on buggy version


```
--- org.jsoup.nodes.Attributes_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[java.lang.Boolean:true |this=org.jsoup.nodes.Attributes:]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Attributes_Zest_Test.test10(Attributes_Zest_Test.java:77)
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
--- org.jsoup.nodes.Attributes_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[org.jsoup.nodes.Attributes: 񆌏󒫹𾇜𺴡񵚚򷋃𖏸󘀬􆼡󬍡󈶹򾳑񝈂򓘍="悦໽ɴ퀌盏ᤍ鋟疰壪竦"] |this=org.jsoup.nod...> but was:<0=[void] |this=org.jsoup.nod...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Attributes_Zest_Test.test12(Attributes_Zest_Test.java:91)
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
--- org.jsoup.nodes.Attributes_Zest_Test::test14
junit.framework.AssertionFailedError: expected:<0=[org.jsoup.nodes.Attributes: 񓶛󒫡𾇜𺴡􊤄="馊㡽퍆쫩猌ꮗǹ멌齖榸䩪ɴ蒣盏ᤍ鋟疰ᶘ"] |this=org.jsoup.nod...> but was:<0=[void] |this=org.jsoup.nod...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
```

- Suite: `/home/user/suites/Jsoup/zest/92/t12001/Jsoup-92f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_92/b120_r1/src/org/jsoup/nodes/Attributes_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_92/b120_r1`
