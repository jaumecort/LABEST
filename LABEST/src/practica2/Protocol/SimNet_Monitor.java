package practica2.Protocol;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.SimNet;

public class SimNet_Monitor implements SimNet {

  protected CircularQueue<TCPSegment> queue;
  final ReentrantLock l = new ReentrantLock();
  final Condition full = l.newCondition();
  final Condition empty = l.newCondition();

  public SimNet_Monitor() {
    queue  = new CircularQueue<>(Const.SIMNET_QUEUE_SIZE);
  }

  @Override
  public void send(TCPSegment seg) {
    l.lock();
    try{
      while (queue.full()) full.awaitUninterruptibly();
      queue.put(seg);
      empty.signalAll();
    } finally {l.unlock();}
  }

  @Override
  public TCPSegment receive() {
    TCPSegment rec;
    l.lock();
    try{
      while (queue.empty()) empty.awaitUninterruptibly();
      rec = queue.get();
      full.signalAll();
    } finally {l.unlock();}
    return rec;
  }

  @Override
  public int getMTU() {
    throw new UnsupportedOperationException("Not supported yet. NO cal completar fins a la pràctica 3...");
  }

}
