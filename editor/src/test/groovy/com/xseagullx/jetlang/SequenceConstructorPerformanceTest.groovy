package com.xseagullx.jetlang

import groovy.util.logging.Log
import org.junit.Test

/** Run with -XX:+UnlockDiagnosticVMOptions -XX:+PrintGCDetails  */
@Log
class SequenceConstructorPerformanceTest {
	@Test void test() throws Exception {
		long lastTime = System.currentTimeMillis()

		Runnable allocateSequence = {
			def currentTimeMillis = System.currentTimeMillis()
			System.out.println(new Sequence(1, 15000000).list[10])
			log.info ("Allocation took: " + System.currentTimeMillis() - currentTimeMillis)
		}

		def thread = new Thread({
			//noinspection GroovyInfiniteLoopStatement
			while (true) {
				def currentTimeMillis = System.currentTimeMillis()
				log.info("Time: " + (currentTimeMillis - lastTime))
				Thread.sleep(1000)
				lastTime = currentTimeMillis
			}
		} as Runnable, "loggingThread")
		thread.start()
		Thread.sleep(5000)
		new Thread(allocateSequence).start()
		thread.join(10000)
	}
}
