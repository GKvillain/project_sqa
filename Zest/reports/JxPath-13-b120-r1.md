# Zest – JxPath-13 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 332270 (valid 88.27%) |
| Cycles | 31 |
| Corpus | 44 |
| Zest branch coverage (total / valid) | 185 / 182 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 44 |
| Failing on fixed | 0 |
| Failing on buggy | 20 |
| Bug detection | Fail (triggering: 20) |
| **Fault detected** | **yes** |
| Line coverage | 55 / 428 = 12.85% |
| Branch coverage | 27 / 315 = 8.57% |
| Test suite length (statements) | 44 |
| Mutation score | 505 / 531 = 95.10% |
| Fuzz time | 120s |
| Pipeline time | 196s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test11
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test12
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test17
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test20
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test21
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test22
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test23
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test25
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test28
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test29
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test30
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test33
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test34
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test38
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test41
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test43
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test5
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test7
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test8
- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test9

```
--- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[null]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test.test11(NamespaceResolver_Zest_Test.java:84)
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
--- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[null |this=org.apache.commons.jxpath.ri.NamespaceResolver:@?]
> but was:<0=[!java.lang.NullPointerException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test.test12(NamespaceResolver_Zest_Test.java:91)
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
--- org.apache.commons.jxpath.ri.NamespaceResolver_Zest_Test::test17
junit.framework.AssertionFailedError: expected:<0=[null |this=org.apache.commons.jxpath.ri.NamespaceResolver:@?
```

- Suite: `/home/user/suites/JxPath/zest/13/t12001/JxPath-13f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_13/b120_r1/src/org/apache/commons/jxpath/ri/NamespaceResolver_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_13/b120_r1`
