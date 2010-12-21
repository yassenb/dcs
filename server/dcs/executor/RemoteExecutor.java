package dcs.executor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteExecutor extends Remote {
    String EXECUTOR_NAME = "executor";

    <T> T executeTask(Task<T> t) throws RemoteException;
}
