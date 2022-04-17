package practica4;

import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

public class TSocket extends TSocket_base {

  //sender variable:
  protected int MSS;

  //receiver variables:
  protected CircularQueue<TCPSegment> rcvQueue;
  protected int rcvSegConsumedBytes;

  protected TSocket(Protocol p, int localPort, int remotePort) {
    super(p.getNetwork());
    this.localPort  = localPort;
    this.remotePort = remotePort;
    p.addActiveTSocket(this);
    MSS = network.getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
    rcvQueue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
    rcvSegConsumedBytes = 0;
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

      segment.setPsh(true);
      segment.setDestinationPort(remotePort);
      segment.setSourcePort(localPort);

      network.send(segment);
    }
  }

  protected TCPSegment segmentize(byte[] data, int offset, int length) {
    TCPSegment segment = new TCPSegment();
    segment.setData(data, offset, length);
    segment.setPsh(true);
    return segment;
  }

  @Override
  public int receiveData(byte[] buf, int offset, int length) {
    lock.lock();
    try {
      while(rcvQueue.empty()){
        appCV.awaitUninterruptibly();
      }
      int rcvbytes = 0;
      while(rcvbytes != length && !rcvQueue.empty()){
        rcvbytes += consumeSegment(buf, offset+rcvbytes, length-rcvbytes);
      }
      return rcvbytes;
    } finally {
      lock.unlock();
      
    }
  }

  protected int consumeSegment(byte[] buf, int offset, int length) {
    TCPSegment seg = rcvQueue.peekFirst();
    int a_agafar = Math.min(length, seg.getDataLength() - rcvSegConsumedBytes);
    System.arraycopy(seg.getData(), rcvSegConsumedBytes, buf, offset, a_agafar);
    rcvSegConsumedBytes += a_agafar;
    if (rcvSegConsumedBytes == seg.getDataLength()) {
      rcvQueue.get();
      rcvSegConsumedBytes = 0;
    }
    return a_agafar;
  }

  protected void sendAck() {
    TCPSegment seg = new TCPSegment();
    seg.setDestinationPort(remotePort);
    seg.setSourcePort(localPort);
    seg.setAck(true);
    network.send(seg);
  }

  @Override
  public void processReceivedSegment(TCPSegment rseg) {

    lock.lock();
    try {
      if(!rcvQueue.full()) {
        printRcvSeg(rseg);
        if (rseg.isAck()){
          //nothing to be done in this exercise.
        } else {
          sendAck();
          rcvQueue.put(rseg);
          appCV.signalAll();
        }
        
      }
      
      
    } finally {
      lock.unlock();
    }
  }

}
