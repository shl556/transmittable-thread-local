package com.alibaba.demo.cow

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val threadPool = Executors.newCachedThreadPool().also {
        TtlExecutors.getTtlExecutorService(it)
    }

    println("${Thread.currentThread().name}: ${traceContext.get()}")

    threadPool.execute {
        traceContext.increaseSpan()
        println("${Thread.currentThread().name}: ${traceContext.get()}")

        threadPool.execute {
            traceContext.increaseSpan()
            println("${Thread.currentThread().name}: ${traceContext.get()}")
        }
    }

    Thread.sleep(100)
    threadPool.shutdown()
    threadPool.awaitTermination(1, TimeUnit.SECONDS)
}

private val traceContext = object : TransmittableThreadLocal<Trace>() {
    override fun initialValue(): Trace = Trace("init", Span("first", 0))

    override fun copy(parentValue: Trace): Trace = parentValue.copy() // 浅拷贝，非常快。没有用COW。避免实现用Wrapper。

    override fun childValue(parentValue: Trace): Trace = parentValue.copy() // 浅拷贝，非常快。没有用COW。避免实现用Wrapper。

    fun increaseSpan() {
        val trace = get()

        // !! COW is HERE!!
        trace.span = trace.span.run {
            copy(id = "$id + PONG", foo = foo + 1)
        }
    }
}

private data class Trace(var name: String, var span: Span)

private data class Span(val id: String, val foo: Int)
