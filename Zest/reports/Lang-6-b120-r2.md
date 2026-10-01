# Zest – Lang-6 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 138366 (valid 83.39%) |
| Cycles | 16 |
| Corpus | 51 |
| Zest branch coverage (total / valid) | 246 / 243 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 51 |
| Failing on fixed | 0 |
| Failing on buggy | 32 |
| Bug detection | Fail (triggering: 32) |
| **Fault detected** | **yes** |
| Line coverage | 23 / 30 = 76.67% |
| Branch coverage | 10 / 12 = 83.33% |
| Test suite length (statements) | 60 |
| Mutation score | 30 / 42 = 71.43% |
| Fuzz time | 121s |
| Pipeline time | 273s |

## Failing tests on buggy version


```
--- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test10
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:񊑴򈐿𪭵𥴹 |this=org.apache.commons.lang3.StringEscapeUtils$CsvEscaper:@?]
> but was:<0=[!java.lang.StringIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test.test10(CharSequenceTranslator_Zest_Test.java:77)
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
--- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test11
junit.framework.AssertionFailedError: expected:<0=[java.lang.String:򤿪򈐿𪭵󟜌𰣶򽥦򅎄󼔡 |this=org.apache.commons.lang3.StringEscapeUtils$CsvEscaper:@?
1=void |a1=java.io.StringWriter:򤿪򈐿𪭵󟜌𰣶򽥦򅎄󼔡 |this=org.apache.commons.lang3.StringEscapeUtils$CsvEscaper:@?]
> but was:<0=[!java.lang.StringIndexOutOfBoundsException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test.test11(CharSequenceTranslator_Zest_Test.java:84)
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
--- org.apache.commons.lang3.text.translate.CharSequenceTranslator_Zest_Test::test13
```

- Suite: `/home/pilaiphon/suites/Lang/zest/6/t12002/Lang-6f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Lang_6/b120_r2/src/org/apache/commons/lang3/text/translate/CharSequenceTranslator_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Lang_6/b120_r2`
