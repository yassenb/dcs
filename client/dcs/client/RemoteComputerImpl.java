package dcs.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import dcs.executor.RemoteExecutor;
import dcs.executor.Task;

public class RemoteComputerImpl implements RemoteComputer {
    private RemoteExecutor executor;

    public RemoteComputerImpl() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }
    
    @Override
    public <T> T computePiece(Task<T> t) throws Exception {
        try {
            initializeExecutor("192.168.1.5");
        } catch (Exception e) {
            throw e;
        }
        
        return executor.executeTask(t);
    }
    
    private void initializeExecutor(String host) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host);
        executor = (RemoteExecutor) registry.lookup(RemoteExecutor.EXECUTOR_NAME);
    }
}
