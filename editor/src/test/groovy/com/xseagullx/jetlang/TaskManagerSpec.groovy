package com.xseagullx.jetlang

import com.xseagullx.jetlang.TaskExecution.Status
import groovy.util.logging.Log
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions
import spock.util.concurrent.PollingConditions

import java.util.function.Consumer

@Log
class SleepTask implements Task {
	@Override void run() {
		log.info("Sleeping... $this")
		sleep(100)
		log.info("Done. $this")
	}
}

class TaskManagerSpec extends Specification {

	TaskManager taskManager = new TaskManager();

	def "task is executed in different thread"() {
		setup:
		def conditions = new AsyncConditions()
		def task = Mock(Task)
		def currentThread = Thread.currentThread()

		when:
		taskManager.run(task)
		conditions.await(1)

		then:
			1 * task.run() >> {
				conditions.evaluate {
					assert currentThread != Thread.currentThread()
				}
			}
	}

	def "Cant' run 2 tasks of the same type"() {
		setup:
		def task1 = new SleepTask()
		def task2 = new SleepTask()

		when:
		taskManager.run(task1)
		taskManager.run(task2)

		then:
		def e = thrown AlreadyRunningException
		e.task == task2
		taskManager.runningTasks().task == [task1]
	}

	def "task is removed when finished"() {
		setup:
		def conditions = new PollingConditions(timeout: 3)
		def task1 = new SleepTask()

		when:
		taskManager.run(task1)

		then:
		taskManager.runningTasks().task == [task1]

		then:
		conditions.eventually {
			assert taskManager.runningTasks() == []
		}
	}

	def "listeners are notified about task lifecycle"() {
		setup:
		List<TaskExecution> list = []
		def conditions = new PollingConditions()
		def task = new SleepTask()
		taskManager.subscribe({ execution ->
			synchronized (list) {
				list << execution.copy()
			}
		} as Consumer<TaskExecution>)

		when:
		taskManager.run(task)

		then:
		conditions.eventually {
			synchronized (list) {
				assert list*.status.containsAll([Status.SCHEDULED, Status.SUCCEEDED])
			}
		}
	}
}
