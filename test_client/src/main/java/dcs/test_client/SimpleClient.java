package dcs.test_client;

import dcs.client.ComputeService;

import java.util.concurrent.Future;

public class SimpleClient {
    public static void main(String[] args) {
        for (int i = 1; i <= 5; ++i) {
            Future<Double> v = ComputeService.submit(new SimpleTask((double)i / 10));
            try {
                System.out.println(v.get());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
