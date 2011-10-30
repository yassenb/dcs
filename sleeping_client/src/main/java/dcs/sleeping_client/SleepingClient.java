package dcs.sleeping_client;

import dcs.client.ComputeService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class SleepingClient {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        int numberOfTasks = Integer.parseInt(args[0]);
        int secondsToSleep = Integer.parseInt(args[1]);
        List<Future<Integer>> tasks = new ArrayList<Future<Integer>>();
		for (int i = 1; i <= numberOfTasks; ++i) {
			tasks.add(ComputeService.submit(new SleepingTask(i, secondsToSleep)));
        }
		
		int i = 1;
		for (Future<Integer> task : tasks) {
			int result;
			try {
				result = task.get();
				if (result != i) {
					System.err.println(String.format("expected % but was %", i, result));
				}
			} catch (Exception e) {
				System.err.println("this can't happen");
			}
            ++i;
		}

        long endTime = System.currentTimeMillis();
        System.out.println(String.format("done in %d seconds", (endTime - startTime) / 1000));
    }
}
