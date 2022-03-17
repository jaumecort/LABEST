package practica2.P1Sync.Monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorSync {

    private final int N;
    static int current_id;
    final ReentrantLock l = new ReentrantLock();;
    final Condition cond = l.newCondition();

    public MonitorSync(int N) {
        current_id = 0;
        this.N = N;
    }

    public void waitForTurn(int id) {
        l.lock();
        while(id != current_id){
            cond.awaitUninterruptibly();
        }
        l.unlock();
    }

    public void transferTurn() {
        l.lock();
        current_id = (current_id + 1)%N;
        cond.signal();
        l.unlock();
    }
}
