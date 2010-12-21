import dcs.client.RemoteComputer;
import dcs.client.RemoteComputerImpl;
import dcs.executor.Task;

public class IntGenerator {
    private RemoteComputer remoteComputer = new RemoteComputerImpl();
    
    public IntGenerator() {
    }
    
    public void compute() throws Exception {
        System.out.println(remoteComputer.computePiece(new SimpleTask()));
    }
   
    public static void main(String args[]) {
        IntGenerator ig = new IntGenerator();
        try {
            ig.compute();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

class SimpleTask implements Task<Integer> {
    private static final long serialVersionUID = 1L;

    public Integer execute() {
        return 3;
    }
}
