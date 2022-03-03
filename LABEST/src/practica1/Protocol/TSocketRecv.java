package practica1.Protocol;

import util.TCPSegment;
import util.TSocket_base;
import util.Log;
import util.SimNet;

public class TSocketRecv extends TSocket_base {

  public TSocketRecv(SimNet net) {
    super(net);
  }

  @Override
  public int receiveData(byte[] data, int offset, int length) {
    TCPSegment segment = network.receive();
    byte[] bytes = segment.getData();
    int tamany = segment.getDataLength();
    if (tamany>length) tamany = length;
    for(int i=0; i<tamany; i++){
      data[offset + i]=bytes[i];
    }
    Log log = Log.getLog();
    log.printPURPLE("\t\t\t\t\t" + segment.toString());
    return tamany;
  }
}
