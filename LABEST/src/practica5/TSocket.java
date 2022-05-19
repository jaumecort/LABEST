package practica5;

import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TSocket_base;
import util.TCPSegment;

public class TSocket extends TSocket_base {

  // Sender variables:
  protected int MSS;
  protected int snd_sndNxt;
  protected int snd_rcvWnd;
  protected int snd_rcvNxt;
  protected TCPSegment snd_UnacknowledgedSeg;
  protected boolean zero_wnd_probe_ON;

  // Receiver variables:
  protected CircularQueue<TCPSegment> rcv_Queue;
  protected int rcv_SegConsumedBytes;
  protected int rcv_rcvNxt;

  protected TSocket(Protocol p, int localPort, int remotePort) {
    super(p.getNetwork());
    this.localPort = localPort;
    this.remotePort = remotePort;
    p.addActiveTSocket(this);
    // init sender variables
    MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
    // init receiver variables
    //rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
    rcv_Queue = new CircularQueue<>(2);
    snd_rcvWnd = Const.RCV_QUEUE_SIZE;
  }

  // -------------  SENDER PART  ---------------
  @Override
  public void sendData(byte[] data, int offset, int length) {
    lock.lock();
    try {
      int bytes_sent = 0;
      while(length - bytes_sent > 0){
        while(snd_sndNxt != snd_rcvNxt){appCV.awaitUninterruptibly();}

        int this_length;
        if (length-bytes_sent>=MSS) this_length = MSS;
        else this_length = length - bytes_sent;
        if(zero_wnd_probe_ON) this_length = 1;
        

        TCPSegment segment = segmentize(data, offset + bytes_sent, this_length);

        bytes_sent = bytes_sent + this_length;

        
        snd_sndNxt++;
        snd_UnacknowledgedSeg = segment;
        network.send(snd_UnacknowledgedSeg);
        startRTO();
    }
    } finally {
      lock.unlock();
    }
  }

  protected TCPSegment segmentize(byte[] data, int offset, int length) {
    TCPSegment segment = new TCPSegment();
    segment.setData(data, offset, length);
    segment.setPsh(true);
    segment.setSeqNum(snd_sndNxt);
    segment.setDestinationPort(remotePort);
    segment.setSourcePort(localPort);
    return segment;
  }

  @Override
  protected void timeout() {
    lock.lock();
    try {
      if(snd_UnacknowledgedSeg != null){
        if(zero_wnd_probe_ON){
          log.printRED("0-wnd-probe: " + snd_UnacknowledgedSeg);
        }
        else{
          log.printPURPLE("  retrans: " + snd_UnacknowledgedSeg);
        }
        network.send(snd_UnacknowledgedSeg);
        startRTO();
      }
    } finally {
      lock.unlock();
      
    }
  }

  // -------------  RECEIVER PART  ---------------
  @Override
  public int receiveData(byte[] buf, int offset, int maxlen) {
    lock.lock();
    try {
      while(rcv_Queue.empty()){
        appCV.awaitUninterruptibly();
      }
      int rcvbytes = 0;
      while(rcvbytes != maxlen && !rcv_Queue.empty()){
        rcvbytes += consumeSegment(buf, offset+rcvbytes, maxlen-rcvbytes);
      }
      return rcvbytes;
    } finally {
      lock.unlock();
      
    }
  }

  protected int consumeSegment(byte[] buf, int offset, int length) {
    TCPSegment seg = rcv_Queue.peekFirst();
    int a_agafar = Math.min(length, seg.getDataLength() - rcv_SegConsumedBytes);
    System.arraycopy(seg.getData(), rcv_SegConsumedBytes, buf, offset, a_agafar);
    rcv_SegConsumedBytes += a_agafar;
    if (rcv_SegConsumedBytes == seg.getDataLength()) {
      rcv_Queue.get();
      rcv_SegConsumedBytes = 0;
    }
    return a_agafar;
  }

  protected void sendAck() {
    TCPSegment seg = new TCPSegment();
    seg.setDestinationPort(remotePort);
    seg.setSourcePort(localPort);
    seg.setAck(true);
    seg.setAckNum(rcv_rcvNxt);
    seg.setWnd(rcv_Queue.free());
    network.send(seg);
  }

  // -------------  SEGMENT ARRIVAL  -------------
  @Override
  public void processReceivedSegment(TCPSegment rseg) {
    lock.lock();
    try {
      if (rseg.isAck()){
        if(rseg.getAckNum() == snd_sndNxt){
          snd_rcvNxt++;
          snd_UnacknowledgedSeg = null;
          printRcvSeg(rseg);

          if(zero_wnd_probe_ON && rseg.getWnd() != 0){
            log.printRED("ZERO-WND-PROBE OFF");
            zero_wnd_probe_ON = false;
          } else if(rseg.getWnd() == 0) {
              log.printRED("ZERO-WND-PROBE ON");
              zero_wnd_probe_ON = true;
          }
          
          
          appCV.signalAll();
        } else {
          log.printRED("ACK NO ESPERADO");
        }
      }

      if(rseg.isPsh()){
        if(!rcv_Queue.full()) {
          if(rcv_rcvNxt == rseg.getSeqNum()){
            printRcvSeg(rseg);
            rcv_Queue.put(rseg);
            rcv_rcvNxt++;
            appCV.signalAll();
          } else {
            log.printRED("\t\t\t\t\t NUM SEQ NO ESPERADO: Paquete descartado.");
          }
          sendAck();
        }
      }

      
    } finally {
      lock.unlock();
    }
  }
}
