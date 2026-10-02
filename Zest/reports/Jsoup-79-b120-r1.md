# Zest – Jsoup-79 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 339618 (valid 96.45%) |
| Cycles | 21 |
| Corpus | 72 |
| Zest branch coverage (total / valid) | 290 / 287 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 72 |
| Failing on fixed | 0 |
| Failing on buggy | 1 |
| Bug detection | Fail (triggering: 1) |
| **Fault detected** | **yes** |
| Line coverage | 34 / 34 = 100.00% |
| Branch coverage | 13 / 14 = 92.86% |
| Test suite length (statements) | 81 |
| Mutation score | 20 / 26 = 76.92% |
| Fuzz time | 120s |
| Pipeline time | 188s |

## Failing tests on buggy version

- org.jsoup.nodes.LeafNode_Zest_Test::test33

```
--- org.jsoup.nodes.LeafNode_Zest_Test::test33
junit.framework.AssertionFailedError: expected:<...:<![CDATA[null]]>
1=[java.util.Collections$EmptyList:[] |this=org.jsoup.nodes.CDataNode:<![CDATA[null]]>]
> but was:<...:<![CDATA[null]]>
1=[!java.lang.UnsupportedOperationException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.LeafNode_Zest_Test.test33(LeafNode_Zest_Test.java:238)
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

- Suite: `/home/user/suites/Jsoup/zest/79/t12001/Jsoup-79f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_79/b120_r1/src/org/jsoup/nodes/LeafNode_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_79/b120_r1`
