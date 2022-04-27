package practica4;

import java.net.Socket;

import util.Protocol_base;
import util.TCPSegment;
import util.SimNet;
import util.TSocket_base;

public class Protocol extends Protocol_base {

    public Protocol(SimNet net) {
      super(net);
    }

    protected void ipInput(TCPSegment seg) {
        TSocket_base sc = getMatchingTSocket(seg.getDestinationPort(), seg.getSourcePort());
        if(sc!=null) sc.processReceivedSegment(seg);
    }

    protected TSocket_base getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            for (TSocket_base sc : activeSockets) {
                if(sc.localPort == localPort && sc.remotePort == remotePort) return sc;
            }
            return null;
        } finally {
            lk.unlock();
        }
    }
}
