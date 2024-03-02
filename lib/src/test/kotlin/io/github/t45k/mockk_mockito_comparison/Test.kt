package io.github.t45k.mockk_mockito_comparison

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.MockKException
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.MockitoKotlinException
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class Test : DescribeSpec({
    val mockkSut = mockk<Sut>()
    val mockitoSut = mock<Sut>()

    afterEach {
        clearMocks(mockkSut)
        clearInvocations(mockitoSut)
    }

    describe("basic") {
        context("mockk") {
            every { mockkSut.hello() } returns WrapObject("hello world")
            it("should mock behavior") {
                mockkSut.hello() shouldBe WrapObject("hello world")
            }
        }

        context("mockito") {
            mockitoSut.stub { on { hello() } doReturn WrapObject("hello world") }
            it("should mock behavior") {
                mockitoSut.hello() shouldBe WrapObject("hello world")
            }
        }
    }

    describe("coroutine") {
        context("mockk") {
            coEvery { mockkSut.helloAsync() } returns WrapObject("hello world async")
            it("should mock behavior") {
                mockkSut.helloAsync() shouldBe WrapObject("hello world async")
            }
        }

        context("mockito") {
            mockitoSut.stub { onBlocking { helloAsync() } doReturn WrapObject("hello world async") }
            it("should mock behavior") {
                mockitoSut.helloAsync() shouldBe WrapObject("hello world async")
            }
        }
    }

    describe("property") {
        context("mock") {
            val sut = mockk<WrapObject> { every { value } returns "hello world" }
            it("should mock property") {
                sut.value shouldBe "hello world"
            }
        }

        context("mockito") {
            val sut = mock<WrapObject> { on { value } doReturn "hello world" }
            it("should create property") {
                sut.value shouldBe "hello world"
            }
        }
    }

    describe("nest") {
        context("mockk") {
            every { mockkSut.hello().value } returns "hello world"
            it("should mock property") {
                mockkSut.hello().value shouldBe "hello world"
            }
        }

        context("mockito") {
            val sut = mock<Sut>(defaultAnswer = RETURNS_DEEP_STUBS) { on { hello().value } doReturn "hello world" }
            it("should mock property") {
                sut.hello().value shouldBe "hello world"
            }
        }
    }

    describe("verify call count") {
        context("mockk") {
            justRun { mockkSut.args(any()) }
            it("should verify") {
                mockkSut.args(WrapObject("hello world"))

                verify { mockkSut.args(WrapObject("hello world")) }
                coVerify(exactly = 0) {
                    mockkSut.hello()
                    mockkSut.helloAsync()
                }
            }
        }

        context("mockito") {
            it("should verify") {
                mockitoSut.args(WrapObject("hello world"))

                verify(mockitoSut) {
                    1 * { args(WrapObject("hello world")) }
                    0 * { hello() }
                    0 * { helloAsync() }
                }
            }
        }
    }

    describe("capture args") {
        context("mockk") {
            val argSlot = slot<WrapObject>()
            justRun { mockkSut.args(capture(argSlot)) }
            it("should capture args") {
                mockkSut.args(WrapObject("hello world"))
                mockkSut.args(WrapObject("good bye"))

                argSlot.captured.value shouldBe "good bye"
            }
        }

        context("mockito") {
            it("should verify") {
                mockitoSut.args(WrapObject("hello world"))
                mockitoSut.args(WrapObject("good bye"))

                verify(mockitoSut) {
                    1 * { args(check { it.value shouldBe "hello world" }) }
                    1 * { args(check { it.value shouldBe "good bye" }) }
                    0 * { args(check { it.value shouldBe "see you" }) }
                }
            }
        }
    }

    describe("value class") {
        context("mockk") {
            it("should create mock") {
                shouldNotThrowAny {
                    mockk<ValueClass>()
                }
            }

            it("should not mock property") {
                shouldThrow<MockKException> {
                    val sut = mockk<ValueClass> { every { value } returns "hello world" }
                    sut.value shouldBe "hello world"
                }
            }
        }

        context("mockito") {
            it("should create mock") {
                shouldNotThrowAny {
                    mock<ValueClass>()
                }
            }

            it("should not mock property") {
                shouldThrow<MockitoKotlinException> {
                    val sut = mock<ValueClass> { on { value } doReturn "hello world" }
                    sut.value shouldBe "hello world"
                }
            }
        }
    }

    describe("value class args") {
        val valueClass = ValueClass("hello world")
        context("mockk") {
            every { mockkSut.valueClass(valueClass) } returns "hello world"
            it("should mock behavior") {
                mockkSut.valueClass(valueClass) shouldBe "hello world"
            }
        }

        context("mockito") {
            mockitoSut.stub { on { valueClass(valueClass) } doReturn "hello world" }
            it("should mock behavior") {
                mockitoSut.valueClass(valueClass) shouldBe "hello world"
            }
        }
    }

    describe("constructor") {
        context("mockk") {
            mockkConstructor()
        }
    }

    describe("object") {
        context("mockk") {
            afterEach { unmockkAll() }
            it("should create mock") {
                mockkObject(Object)
                every { Object.helloWorld() } returns "good bye"

                Object.helloWorld() shouldBe "good bye"
            }
        }

        context("mockito") {
            // need @JvmStatic
            it("should create mock") {
                mockStatic(Object::class.java).use {
                    it.`when`<String> { Object.helloWorld() } doReturn "good bye"

                    Object.helloWorld() shouldBe "good bye"
                }
            }
        }
    }

    describe("top level function") {
        context("mockk") {
            it("should mock top level function") {
                mockkStatic(::topLevelFunction)
                justRun { topLevelFunction() }

                shouldNotThrowAny {
                    topLevelFunction()
                }
            }
            unmockkAll()
        }

        context("mockito") {
            // Currently, mocking top level function of Kotlin is not supported by Mockito core
            // https://github.com/mockito/mockito/issues/1468
        }
    }
})
