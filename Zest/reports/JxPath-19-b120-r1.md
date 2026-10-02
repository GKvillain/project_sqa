# Zest – JxPath-19 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 363098 (valid 66.62%) |
| Cycles | 25 |
| Corpus | 45 |
| Zest branch coverage (total / valid) | 195 / 192 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 45 |
| Failing on fixed | 0 |
| Failing on buggy | 7 |
| Bug detection | Fail (triggering: 7) |
| **Fault detected** | **yes** |
| Line coverage | 55 / 734 = 7.49% |
| Branch coverage | 28 / 573 = 4.89% |
| Test suite length (statements) | 45 |
| Mutation score | 774 / 798 = 96.99% |
| Fuzz time | 120s |
| Pipeline time | 190s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test10
- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test13
- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test22
- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test28
- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test33
- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test42
- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test8

```
--- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test.test10(DOMNodePointer_Zest_Test.java:77)
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
--- org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<...ng.Boolean:false
1=![java.lang.NullPointer]Exception
> but was:<...ng.Boolean:false
1=![H:java.lang.NoSuchMethod]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.model.dom.DOMNodePointer_Zest_Test.test13(DOMNodePointer_Zest_Test.java:98)
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

- Suite: `/home/user/suites/JxPath/zest/19/t12001/JxPath-19f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_19/b120_r1/src/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_19/b120_r1`
