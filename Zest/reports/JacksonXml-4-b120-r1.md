# Zest – JacksonXml-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 477996 (valid 42.12%) |
| Cycles | 26 |
| Corpus | 28 |
| Zest branch coverage (total / valid) | 168 / 165 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 28 |
| Failing on fixed | 0 |
| Failing on buggy | 4 |
| Bug detection | Fail (triggering: 4) |
| **Fault detected** | **yes** |
| Line coverage | 21 / 122 = 17.21% |
| Branch coverage | 6 / 60 = 10.00% |
| Test suite length (statements) | 28 |
| Mutation score | 78 / 82 = 95.12% |
| Fuzz time | 127s |
| Pipeline time | 182s |

## Failing tests on buggy version

- com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test::test13
- com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test::test16
- com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test::test21
- com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test::test6

```
--- com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test::test13
junit.framework.AssertionFailedError: expected:<0=![java.lang.NullPointer]Exception
> but was:<0=![com.fasterxml.jackson.databind.JsonMapping]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test.test13(XmlSerializerProvider_Zest_Test.java:98)
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
--- com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test::test16
junit.framework.AssertionFailedError: expected:<...lizerProvider:@?
1=![java.lang.NullPointer]Exception
> but was:<...lizerProvider:@?
1=![com.fasterxml.jackson.databind.JsonMapping]Exception
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider_Zest_Test.test16(XmlSerializerProvider_Zest_Test.java:119)
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

- Suite: `/home/user/suites/JacksonXml/zest/4/t12001/JacksonXml-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/JacksonXml_4/b120_r1/src/com/fasterxml/jackson/dataformat/xml/ser/XmlSerializerProvider_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/JacksonXml_4/b120_r1`
