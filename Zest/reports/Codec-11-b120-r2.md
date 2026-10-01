# Zest – Codec-11 (budget 120s, run 2)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 115214 (valid 86.21%) |
| Cycles | 5 |
| Corpus | 108 |
| Zest branch coverage (total / valid) | 305 / 303 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 108 |
| Failing on fixed | 0 |
| Failing on buggy | 53 |
| Bug detection | Fail (triggering: 53) |
| **Fault detected** | **yes** |
| Line coverage | 95 / 107 = 88.79% |
| Branch coverage | 62 / 70 = 88.57% |
| Test suite length (statements) | 108 |
| Mutation score | 164 / 199 = 82.41% |
| Fuzz time | 120s |
| Pipeline time | 202s |

## Failing tests on buggy version

- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test102
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test103
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test105
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test107
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test10
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test13
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test15
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test18
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test20
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test22
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test24
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test25
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test27
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test31
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test34
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test39
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test40
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test44
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test45
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test47
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test51
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test52
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test53
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test54
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test55
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test56
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test57
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test58
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test59
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test60
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test63
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test66
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test67
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test68
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test70
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test71
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test75
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test77
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test78
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test80
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test83
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test87
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test88
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test89
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test91
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test92
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test96
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test97
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test98
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test99
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test5
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test6
- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test8

```
--- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test102
junit.framework.AssertionFailedError: expected:<...=B0=A8=00=E8=88=90W=[
=F2=A4=80=87=F3=82=83=84=F3=97=B8=87=F3=BD=90=BE=F2=94=96=A3 |this=org.apache.commons.codec.net.QuotedPrintableCodec:@?
1=java.lang.Boolean:false]
> but was:<...=B0=A8=00=E8=88=90W=[F2=A4=80=87=F3=82=83=84=F3=97=B8=87=F3=BD=90=BE=F2=94=96=A3 |this=org.apache.commons.codec.net.QuotedPrintableCodec:@?
1=!H:java.lang.NoSuchMethodException]
>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test.test102(QuotedPrintableCodec_Zest_Test.java:721)
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
--- org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test::test103
junit.framework.AssertionFailedError: expected:<...1=F3=84=90=B4=F4=81=[
=83=88=F1=A9=BE=BC=F2=8B=B7=93=F2=9A=A2=84=F0=95=81=9B=F1=92=89=89=F3=A4=84=
=A6=F0=92=A8=B1=F1=80=8E=BB=F0=B2=AA=90=F2=AD=BB=A9=F3=B7=8E=92=F0=A4=B4=A0=
=F1=BF=A0=99=F0=90=84=B3=F3=8B=BA=AA=F1=82=8E=A7=EE=96=87=F3=B2=97... |this=org.apache.commons.codec.net.QuotedPrintableCodec:@?
1=java.lang.String:=F2=84=B4=9E=E9=8D=9B=F3=A5=99=B6=F3=8E=B4=B5=F2=90=AE=91=F3=84=90=B4=F4=81=
=83=88=F1=A9=BE=BC=F2=8B=B7=93=F2=9A=A2=84=F0=95=81=9B=F1=92=89=89=F3=A4=84=
=A6=F0=92=A8=B1=F1=80=8E=BB=F0=B2=AA=90=F2=AD=BB=A9=F3=B7=8E=92=F0=A4=B4=A0=
=F1=BF=A0=99=F0=90=84=B3=F3=8B=BA=AA=F1=82=8E=A7=EE=96=87=F3=B2=97]... |this=org.apache...> but was:<...1=F3=84=90=B4=F4=81=[83=88=F1=A9=BE=BC=F2=8B=B7=93=F2=9A=A2=84=F0=95=81=9B=F1=92=89=89=F3=A4=84=A6=F0=92=A8=B1=F1=80=8E=BB=F0=B2=AA=90=F2=AD=BB=A9=F3=B7=8E=92=F0=A4=B4=A0=F1=BF=A0=99=F0=90=84=B3=F3=8B=BA=AA=F1=82=8E=A7=EE=96=87=F3=B2=97=82=F0=AD... |this=org.apache.commons.codec.net.QuotedPrintableCodec:@?
1=java.lang.String:=F2=84=B4=9E=E9=8D=9B=F3=A5=99=B6=F3=8E=B4=B5=F2=90=AE=91=F3=84=90=B4=F4=81=83=88=F1=A9=BE=BC=F2=8B=B7=93=F2=9A=A2=84=F0=95=81=9B=F1=92=89=89=F3=A4=84=A6=F0=92=A8=B1=F1=80=8E=BB=F0=B2=AA=90=F2=AD=BB=A9=F3=B7=8E=92=F0=A4=B4=A0=F1=BF=A0=99=F0=90=84=B3=F3=8B=BA=AA=F1=82=8E=A7=EE=96=87=F3=B2=97=82=F0=AD]... |this=org.apache...>
	at org.junit.Assert.assertEquals(Assert.java:115)
	at org.junit.Assert.assertEquals(Assert.java:144)
	at org.apache.commons.codec.net.QuotedPrintableCodec_Zest_Test.test103(QuotedPrintableCodec_Zest_Test.java:728)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
```

- Suite: `/home/pilaiphon/suites/Codec/zest/11/t12002/Codec-11f-zest.12002.tar.bz2`
- Tests: `/home/pilaiphon/zest-d4j/gen/Codec_11/b120_r2/src/org/apache/commons/codec/net/QuotedPrintableCodec_Zest_Test.java`
- Corpus: `/home/pilaiphon/zest-d4j/out/Codec_11/b120_r2`
