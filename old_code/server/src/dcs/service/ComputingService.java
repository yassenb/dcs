package dcs.service;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import dcs.executor.RemoteExecutor;
import dcs.executor.Task;

class ComputingService implements RemoteExecutor {
    private static final int EXPORT_OBJEXT_PORT = 55555;
    private ComputingService() {
    }
    
    @Override
    public <T> T executeTask(Task<T> t) throws RemoteException {
        return t.execute();
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        try {
            RemoteExecutor engine = new ComputingService();
            RemoteExecutor stub = (RemoteExecutor) UnicastRemoteObject.exportObject(engine, EXPORT_OBJEXT_PORT);
            Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
            registry.rebind(RemoteExecutor.EXECUTOR_NAME, stub);
            System.out.println("ComputingService bound");
        } catch (Exception e) {
            System.err.println("ComputingService exception:");
            System.err.println(e.getMessage());
        }
    }
}
