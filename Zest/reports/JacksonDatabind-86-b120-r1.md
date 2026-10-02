# Zest – JacksonDatabind-86 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 380547 (valid 94.98%) |
| Cycles | 40 |
| Corpus | 33 |
| Zest branch coverage (total / valid) | 169 / 166 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 33 |
| Failing on fixed | 0 |
| Failing on buggy | 3 |
| Bug detection | Fail (triggering: 3) |
| **Fault detected** | **yes** |
| Line coverage | 21 / 33 = 63.64% |
| Branch coverage | 3 / 16 = 18.75% |
| Test suite length (statements) | 100 |
| Mutation score | 17 / 22 = 77.27% |
| Fuzz time | 126s |
| Pipeline time | 239s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test26
- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test6
- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test8

```
--- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test26
junit.framework.AssertionFailedError: expected:<0=[null |this=com.fasterxml.jackson.databind.type.ResolvedRecursiveType:[recursive type; UNRESOLVED
1=null |this=com.fasterxml.jackson.databind.type.ResolvedRecursiveType:[recursive type; UNRESOLVED]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test.test26(ResolvedRecursiveType_Zest_Test.java:189)
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
--- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test6
junit.framework.AssertionFailedError: expected:<0=[null |this=com.fasterxml.jackson.databind.type.ResolvedRecursiveType:[recursive type; UNRESOLVED]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test.test6(ResolvedRecursiveType_Zest_Test.java:49)
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
--- com.fasterxml.jackson.databind.type.ResolvedRecursiveType_Zest_Test::test8
```

- Suite: `/home/user/suites/JacksonDatabind/zest/86/t12001/JacksonDatabind-86f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_86/b120_r1/src/com/fasterxml/jackson/databind/type/ResolvedRecursiveType_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_86/b120_r1`
