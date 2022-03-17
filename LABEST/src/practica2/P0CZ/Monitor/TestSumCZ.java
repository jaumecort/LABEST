package practica2.P0CZ.Monitor;

public class TestSumCZ {

    public static void main(String[] args) throws InterruptedException {
        MonitorCZ mon = new MonitorCZ();
        CounterThreadCZ th_1 = new CounterThreadCZ(mon);
        CounterThreadCZ th_2 = new CounterThreadCZ(mon);

        th_1.start();
        th_2.start();
        //Thread.sleep(100);
        //System.out.println(mon.getX());
    }
}
