package dcs.client;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import dcs.executor.RemoteExecutor;
import dcs.executor.Task;

public class RemoteComputerImpl implements RemoteComputer {
    private RemoteExecutor executor;

    public RemoteComputerImpl() {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        sqliteTest();
    }
    
    // TODO remove me
    private void sqliteTest() {
        Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.WARNING);
        SQLiteConnection db = new SQLiteConnection(new File("/tmp/dcs.db"));
        try {
            db.open(false);
            SQLiteStatement st = db.prepare("SELECT ip FROM servers", false);
            while (st.step()) {
                System.out.println(st.columnString(0));
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.dispose();
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
        Registry registry = LocateRegistry.getRegistry(host, RemoteExecutor.REGISTRY_PORT);
        executor = (RemoteExecutor) registry.lookup(RemoteExecutor.EXECUTOR_NAME);
    }
}
