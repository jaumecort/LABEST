package practica3;

import java.util.Random;
import util.Const;
import util.Log;
import util.TCPSegment;

public class SimNet_Loss extends practica2.Protocol.SimNet_Monitor {

  private double lossRate;
  private Random rand;
  private Log log;

  public SimNet_Loss(double lossRate) {
    this.lossRate = lossRate;
    rand = new Random(Const.SEED);
    log = Log.getLog();
  }

  @Override
  public void send(TCPSegment seg) {
    l.lock();
    try{
      if(rand.nextDouble()<lossRate){log.printRED("Segment Lost");}
      else{
        while (queue.full()) full.awaitUninterruptibly();
        queue.put(seg);
        //log.printPURPLE(seg.toString());
        empty.signalAll();
      }
    } finally {l.unlock();}
  }

  @Override
  public int getMTU() {
    return Const.MTU_ETHERNET;
  }
}
