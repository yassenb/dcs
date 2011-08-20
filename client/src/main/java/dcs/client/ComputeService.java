package dcs.client;

import dcs.common.Task;

import java.io.Serializable;
import java.util.concurrent.Future;

public class ComputeService {
    public static <T extends Serializable> Future<T> submit(Task<T> task) {
        // I don't know what this black magic is but it's necessary to call the companion object's method and not the
        // same-named class method
        return DistributedComputeService$.MODULE$.submit(task);
    }
}
