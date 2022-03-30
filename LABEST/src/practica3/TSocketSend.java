package practica3;

import util.Const;
import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketSend extends TSocket_base {

  protected int MSS;       // Maximum Segment Size

  public TSocketSend(SimNet net) {
    super(net);
    MSS = net.getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
  }

  @Override
  public void sendData(byte[] data, int offset, int length) {

    int bytes_sent = 0;
    while(length - bytes_sent > 0){

      int this_length;
      if (length-bytes_sent>=MSS) this_length = MSS;
      else this_length = length - bytes_sent;

      TCPSegment segment = segmentize(data, offset + bytes_sent, this_length);

      bytes_sent = bytes_sent + this_length;

      network.send(segment);
    }

    
  }

  protected TCPSegment segmentize(byte[] data, int offset, int length) {
      TCPSegment segment = new TCPSegment();
      segment.setData(data, offset, length);
      segment.setPsh(true);
      return segment;
  }

}
