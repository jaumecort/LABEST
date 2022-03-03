package practica1.Protocol;

import util.TCPSegment;
import util.TSocket_base;
import util.Log;
import util.SimNet;

public class TSocketSend extends TSocket_base {

  public TSocketSend(SimNet net) {
    super(net);
  }

  @Override
  public void sendData(byte[] data, int offset, int length) {
    TCPSegment segment = new TCPSegment();
    Log log = Log.getLog();
    segment.setData(data, offset, length);
    segment.setPsh(true);
    network.send(segment);
    log.printPURPLE(segment.toString());
  }
}
