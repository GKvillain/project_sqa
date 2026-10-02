# Zest – JxPath-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 360379 (valid 76.64%) |
| Cycles | 31 |
| Corpus | 29 |
| Zest branch coverage (total / valid) | 174 / 171 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 29 |
| Failing on fixed | 0 |
| Failing on buggy | 8 |
| Bug detection | Fail (triggering: 8) |
| **Fault detected** | **yes** |
| Line coverage | 24 / 83 = 28.92% |
| Branch coverage | 3 / 34 = 8.82% |
| Test suite length (statements) | 32 |
| Mutation score | 85 / 91 = 93.41% |
| Fuzz time | 120s |
| Pipeline time | 194s |

## Failing tests on buggy version

- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test10
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test16
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test17
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test19
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test21
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test23
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test26
- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test4

```
--- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[org.apache.commons.jxpath.JXPathAbstractFactoryException:org.apache.commons.jxpath.JXPathAbstractFactoryException: Factory null reported success creating object for path: /* but object was null.  Terminating to avoid stack recursion. |this=org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer:/*]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test.test10(NullPropertyPointer_Zest_Test.java:77)
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
--- org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<...ropertyPointer:/*
1=[org.apache.commons.jxpath.JXPathAbstractFactoryException:org.apache.commons.jxpath.JXPathAbstractFactoryException: Factory null reported success creating object for path: /* but object was null.  Terminating to avoid stack recursion. |this=org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer:/*]
> but was:<...ropertyPointer:/*
1=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer_Zest_Test.test16(NullPropertyPointer_Zest_Test.java:119)
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

- Suite: `/home/user/suites/JxPath/zest/3/t12001/JxPath-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JxPath_3/b120_r1/src/org/apache/commons/jxpath/ri/model/beans/NullPropertyPointer_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JxPath_3/b120_r1`
