package dcs.executor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteExecutor extends Remote {
    String EXECUTOR_NAME = "executor";
    int REGISTRY_PORT = 55556;

    <T> T executeTask(Task<T> t) throws RemoteException;
}
