package practica2.P0CZ;

public class TestSum {

    public static void main(String[] args) throws InterruptedException {
        CounterThread c1 = new CounterThread();
        CounterThread c2 = new CounterThread();
        c1.start();
        c2.start();
        
        System.out.println("Resultat: " + CounterThread.x);
    }
}
