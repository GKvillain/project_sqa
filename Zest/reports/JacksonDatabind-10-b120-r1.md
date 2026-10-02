# Zest – JacksonDatabind-10 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 388308 (valid 62.37%) |
| Cycles | 27 |
| Corpus | 35 |
| Zest branch coverage (total / valid) | 185 / 182 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 35 |
| Failing on fixed | 0 |
| Failing on buggy | 19 |
| Bug detection | Fail (triggering: 19) |
| **Fault detected** | **yes** |
| Line coverage | 23 / 285 = 8.07% |
| Branch coverage | 2 / 172 = 1.16% |
| Test suite length (statements) | 35 |
| Mutation score | 195 / 198 = 98.48% |
| Fuzz time | 128s |
| Pipeline time | 227s |

## Failing tests on buggy version

- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test10
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test12
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test15
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test19
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test21
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test22
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test23
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test24
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test25
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test26
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test27
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test30
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test31
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test33
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test34
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test0
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test1
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test5
- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test6

```
--- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[void |this=com.fasterxml.jackson.databind.ser.AnyGetterWriter:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test.test10(AnyGetterWriter_Zest_Test.java:77)
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
--- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test12
junit.framework.AssertionFailedError: expected:<0=[new com.fasterxml.jackson.databind.ser.AnyGetterWriter:@?]
> but was:<0=[!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test.test12(AnyGetterWriter_Zest_Test.java:91)
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
--- com.fasterxml.jackson.databind.ser.AnyGetterWriter_Zest_Test::test15
junit.framework.AssertionFailedError: expected:<0=[new com.fasterxml.jackson.databind.ser.AnyGetterWriter:@?
```

- Suite: `/home/user/suites/JacksonDatabind/zest/10/t12001/JacksonDatabind-10f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonDatabind_10/b120_r1/src/com/fasterxml/jackson/databind/ser/AnyGetterWriter_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonDatabind_10/b120_r1`
