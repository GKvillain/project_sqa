# Zest – Jsoup-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 323669 (valid 81.52%) |
| Cycles | 30 |
| Corpus | 40 |
| Zest branch coverage (total / valid) | 271 / 264 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 40 |
| Failing on fixed | 0 |
| Failing on buggy | 36 |
| Bug detection | Fail (triggering: 36) |
| **Fault detected** | **yes** |
| Line coverage | 62 / 140 = 44.29% |
| Branch coverage | 15 / 42 = 35.71% |
| Test suite length (statements) | 231 |
| Mutation score | 118 / 149 = 79.19% |
| Fuzz time | 120s |
| Pipeline time | 185s |

## Failing tests on buggy version

- org.jsoup.nodes.Node_Zest_Test::test10
- org.jsoup.nodes.Node_Zest_Test::test11
- org.jsoup.nodes.Node_Zest_Test::test12
- org.jsoup.nodes.Node_Zest_Test::test13
- org.jsoup.nodes.Node_Zest_Test::test14
- org.jsoup.nodes.Node_Zest_Test::test15
- org.jsoup.nodes.Node_Zest_Test::test17
- org.jsoup.nodes.Node_Zest_Test::test18
- org.jsoup.nodes.Node_Zest_Test::test19
- org.jsoup.nodes.Node_Zest_Test::test20
- org.jsoup.nodes.Node_Zest_Test::test22
- org.jsoup.nodes.Node_Zest_Test::test23
- org.jsoup.nodes.Node_Zest_Test::test24
- org.jsoup.nodes.Node_Zest_Test::test25
- org.jsoup.nodes.Node_Zest_Test::test26
- org.jsoup.nodes.Node_Zest_Test::test27
- org.jsoup.nodes.Node_Zest_Test::test28
- org.jsoup.nodes.Node_Zest_Test::test29
- org.jsoup.nodes.Node_Zest_Test::test30
- org.jsoup.nodes.Node_Zest_Test::test31
- org.jsoup.nodes.Node_Zest_Test::test32
- org.jsoup.nodes.Node_Zest_Test::test33
- org.jsoup.nodes.Node_Zest_Test::test35
- org.jsoup.nodes.Node_Zest_Test::test36
- org.jsoup.nodes.Node_Zest_Test::test37
- org.jsoup.nodes.Node_Zest_Test::test38
- org.jsoup.nodes.Node_Zest_Test::test39
- org.jsoup.nodes.Node_Zest_Test::test0
- org.jsoup.nodes.Node_Zest_Test::test1
- org.jsoup.nodes.Node_Zest_Test::test2
- org.jsoup.nodes.Node_Zest_Test::test3
- org.jsoup.nodes.Node_Zest_Test::test4
- org.jsoup.nodes.Node_Zest_Test::test5
- org.jsoup.nodes.Node_Zest_Test::test7
- org.jsoup.nodes.Node_Zest_Test::test8
- org.jsoup.nodes.Node_Zest_Test::test9

```
--- org.jsoup.nodes.Node_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<...jsoup.nodes.Comment:[
<!--񪍐𯐡󺈠񄬠-->
1=org.jsoup.nodes.Attributes: comment="&#55656;&#57168;&#55421;&#56353;&#56232;&#56864;&#55506;&#57120;" |this=org.jsoup.nodes.Comment:
<!--񪍐𯐡󺈠񄬠-->]
> but was:<...jsoup.nodes.Comment:[!toString:java.lang.NullPointerException
1=org.jsoup.nodes.Attributes: comment="&#55656;&#57168;&#55421;&#56353;&#56232;&#56864;&#55506;&#57120;" |this=org.jsoup.nodes.Comment:!toString:java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Node_Zest_Test.test10(Node_Zest_Test.java:77)
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
--- org.jsoup.nodes.Node_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<...jsoup.nodes.Comment:[
<!--򌔐𾈡󺈠񄬠񐐱𝌒𔧸򶶑򠅭򚧥-->]
> but was:<...jsoup.nodes.Comment:[!toString:java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.jsoup.nodes.Node_Zest_Test.test11(Node_Zest_Test.java:84)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
```

- Suite: `/home/user/suites/Jsoup/zest/8/t12001/Jsoup-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Jsoup_8/b120_r1/src/org/jsoup/nodes/Node_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Jsoup_8/b120_r1`
