package dcs.sleeping_client;

import dcs.common.Task;

@SuppressWarnings("serial")
class SleepingTask implements Task<Integer> {
    private int id;
    private int seconds;

    SleepingTask(int id, int seconds) {
        this.id = id;
        this.seconds = seconds;
    }

    public Integer execute() {
        try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
		}
        return id;
    }
}
