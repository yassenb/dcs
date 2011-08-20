package dcs.test_client;

import dcs.common.Task;

class SimpleTask implements Task<Double> {
    private double a;

    SimpleTask(double a) {
        this.a = a;
    }

    public Double execute() {
        return a;
    }
}