# Zest – Jsoup-49 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 458459 (valid 66.75%) |
| Cycles | 25 |
| Corpus | 66 |
| Zest branch coverage (total / valid) | 303 / 297 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 66 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 68 / 231 = 29.44% |
| Branch coverage | 15 / 102 = 14.71% |
| Test suite length (statements) | 90 |
| Mutation score | 211 / 246 = 85.77% |
| Fuzz time | 120s |
| Pipeline time | 191s |

## Failing tests on buggy version

- org.jsoup.nodes.Node_Zest_Test::test48

```
--- org.jsoup.nodes.Node_Zest_Test::test48
junit.framework.AssertionFailedError: expected:<0=[void |a1=[Lorg.jsoup.nodes.Node;:[[]] |this=org.jsoup.nodes.Comment:
<!--񹂜-->
1=]!java.lang.IndexOutO...> but was:<0=[]!java.lang.IndexOutO...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Node_Zest_Test.test48(Node_Zest_Test.java:343)
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

- Suite: `/home/user/suites/Jsoup/zest/49/t12001/Jsoup-49f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_49/b120_r1/src/org/jsoup/nodes/Node_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_49/b120_r1`
