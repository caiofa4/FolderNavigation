package com.accessibility.accessibilityfolder.helper

import android.util.Log
import com.accessibility.accessibilityfolder.TAG
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CountdownTimer {
    private lateinit var latch: CountDownLatch
    private lateinit var executor: ScheduledExecutorService
    private lateinit var remainingTime: AtomicInteger

    fun start(countdownDuration: Int) {
        latch = CountDownLatch(1)
        executor = Executors.newSingleThreadScheduledExecutor()
        remainingTime = AtomicInteger(countdownDuration + 1)

        val countdownTask = Runnable {
            val time = remainingTime.decrementAndGet()
            Log.i(TAG, "remainingTime $time")
            if (time > 0) {
//                timerCountdown.intValue = time
            } else {
                executor.shutdown()
                latch.countDown()
            }
        }
        executor.scheduleAtFixedRate(countdownTask, 0, 1, TimeUnit.SECONDS)
        // Main thread waits until countdown finishes
        latch.await()
    }
}