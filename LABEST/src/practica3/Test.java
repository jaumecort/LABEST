package practica3;

import util.Receiver;
import util.Sender;
import util.SimNet;
//import practica2.Protocol.SimNet_Monitor;

public class Test {

    public static void main(String[] args) {
        SimNet net = new SimNet_Loss(0.001);
        new Sender(new TSocketSend(net), 6, 3000, 100).start();
        new Receiver(new TSocketRecv(net), 2000, 200).start();
    }
}
