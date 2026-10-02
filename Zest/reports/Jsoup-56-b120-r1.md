# Zest – Jsoup-56 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 343009 (valid 72.03%) |
| Cycles | 18 |
| Corpus | 93 |
| Zest branch coverage (total / valid) | 332 / 327 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 93 |
| Failing on fixed | 0 |
| Failing on buggy | 18 |
| Bug detection | Fail (triggering: 18) |
| **Fault detected** | **yes** |
| Line coverage | 259 / 2511 = 10.31% |
| Branch coverage | 20 / 1175 = 1.70% |
| Test suite length (statements) | 93 |
| Mutation score | 2328 / 2344 = 99.32% |
| Fuzz time | 121s |
| Pipeline time | 198s |

## Failing tests on buggy version


```
--- org.jsoup.nodes.DocumentType_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<0=[new org.jsoup.nodes.DocumentType:<!DOCTYPE 󟑦𼾅񎱋𠘷𹲪򈺘񵥣􅈾򴤚 龞페ಷ㾌딨著ꞎ❘⑆ᗤ挤각ꓢ氟 "򔠏󭄑󿴠򮃭" "󟑦𼾅񎱋𠘷𹲪򈺘񵥣􅈾򴤚">
1=void |this=org.jsoup.nodes.DocumentType:<!DOCTYPE 󟑦𼾅񎱋𠘷𹲪򈺘񵥣􅈾򴤚 PUBLIC "龞페ಷ㾌딨著ꞎ❘⑆ᗤ挤각ꓢ氟" "򔠏󭄑󿴠򮃭">]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.DocumentType_Zest_Test.test16(DocumentType_Zest_Test.java:119)
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
--- org.jsoup.nodes.DocumentType_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<...⟉绵⑆䈻挤柨࣫䨵" "򐬝󭄑">
1=[new org.jsoup.nodes.DocumentType:<!DOCTYPE 󨝔򡮀񀹿𠘷𹲪䌖񷽡򱲧򨄺 龞페ಷ㾌딨㲈⟉绵⑆䈻挤柨࣫䨵 "򐬝󭄑" "󨝔򡮀񀹿𠘷𹲪䌖񷽡򱲧򨄺">]
> but was:<...⟉绵⑆䈻挤柨࣫䨵" "򐬝󭄑">
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.DocumentType_Zest_Test.test17(DocumentType_Zest_Test.java:126)
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

- Suite: `/home/user/suites/Jsoup/zest/56/t12001/Jsoup-56f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_56/b120_r1/src/org/jsoup/nodes/DocumentType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_56/b120_r1`
