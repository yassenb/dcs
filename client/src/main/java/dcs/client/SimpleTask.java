package dcs.client;

import dcs.common.Task;

public class SimpleTask implements Task<Double> {
    private double a;

    SimpleTask(double a) {
        this.a = a;
    }

    public Double execute() {
        return a;
    }
}