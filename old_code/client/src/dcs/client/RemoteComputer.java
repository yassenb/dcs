package dcs.client;

import dcs.executor.Task;

public interface RemoteComputer {
    <T> T computePiece(Task<T> t) throws Exception;
}
