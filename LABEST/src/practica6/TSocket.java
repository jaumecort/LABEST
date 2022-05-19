package practica6;

//import java.security.AllPermission;
import java.util.Iterator;
import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

public class TSocket extends TSocket_base {

    // Sender variables:
    protected int MSS;
    protected int snd_sndNxt;
    protected int snd_rcvNxt;
    protected int snd_rcvWnd;
    protected int snd_cngWnd;
    protected int snd_minWnd;
    protected CircularQueue<TCPSegment> snd_unacknowledged_segs;
    protected boolean zero_wnd_probe_ON;

    // Receiver variables:
    protected int rcv_rcvNxt;
    protected CircularQueue<TCPSegment> rcv_Queue;
    protected int rcv_SegConsumedBytes;

    @SuppressWarnings("Type")
    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        // init sender variables:
        MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        MSS = 10;
        // init receiver variables:
        rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        snd_rcvWnd = Const.RCV_QUEUE_SIZE;
        snd_cngWnd = 3;
        snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);
        snd_unacknowledged_segs = new CircularQueue<>(snd_cngWnd);
    }

    // -------------  SENDER PART  ---------------
    @Override
    public void sendData(byte[] data, int offset, int length) {
        lock.lock();
        try {
        int bytes_sent = 0;
        while(length - bytes_sent > 0){

            snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);
            
            while((snd_unacknowledged_segs.size()>=snd_minWnd && snd_rcvWnd!=0) || snd_unacknowledged_segs.full()){
              appCV.awaitUninterruptibly();
            }

            if(zero_wnd_probe_ON && snd_rcvWnd > 0){
              log.printRED("ZERO-WND-PROBE OFF");
              zero_wnd_probe_ON = false;
            } else if(snd_rcvWnd == 0) {
              log.printRED("ZERO-WND-PROBE ON");
              zero_wnd_probe_ON = true;
            }

            int this_length;
            if (length-bytes_sent>=MSS) this_length = MSS;
            else this_length = length - bytes_sent;
            if(zero_wnd_probe_ON) this_length = 1;
            

            TCPSegment segment = segmentize(data, offset + bytes_sent, this_length);
            bytes_sent = bytes_sent + this_length;
            snd_sndNxt++;
            snd_unacknowledged_segs.put(segment);
            log.printBLUE("send: " + segment.toString());
            network.send(segment);
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
        if(!snd_unacknowledged_segs.empty()){
            for (TCPSegment tcpSegment : snd_unacknowledged_segs) {
                log.printRED("retrans: " + tcpSegment.toString());
                network.send(tcpSegment);
            }
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
        log.printBLUE("\t\t\t\tsend: "+seg.toString());
        network.send(seg);
    }

    // -------------  SEGMENT ARRIVAL  -------------
    public void processReceivedSegment(TCPSegment rseg) {

        lock.lock();
        try {
          // ACK
          if (rseg.isAck()){
              log.printPURPLE("reveived: " + rseg.toString());

              // Actualizar variables
              snd_rcvNxt = rseg.getAckNum();
              snd_rcvWnd = rseg.getWnd();

              //ACKNOWLEDGE
              Iterator ite = snd_unacknowledged_segs.iterator();
              while(ite.hasNext()){
                  TCPSegment next = (TCPSegment) ite.next();
                  if(next.getSeqNum()<snd_rcvNxt) ite.remove();
              }

              snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);
              
              stopRTO();
              if(!snd_unacknowledged_segs.empty()) startRTO();
              appCV.signalAll();
          }

          // PSH
          if(rseg.isPsh()){
            if(!rcv_Queue.full()) {
              printRcvSeg(rseg);
              if(rcv_rcvNxt == rseg.getSeqNum()){
                rcv_Queue.put(rseg);
                rcv_rcvNxt++;
                appCV.signalAll();
              } else log.printRED("\t\t\t\t\t NUM SEQ NO ESPERADO: Paquete descartado.");
              sendAck();
            }
          }
    
          
        } finally {
          lock.unlock();
        }
    }
    
    private void unacknowledgedSegments_content() {
        Iterator<TCPSegment> ite = snd_unacknowledged_segs.iterator();
        log.printBLACK("\n-------------- content begins  --------------");
        while (ite.hasNext()) {
            log.printBLACK(ite.next().toString());
        }
        log.printBLACK("-------------- content ends    --------------\n");
    }
}
