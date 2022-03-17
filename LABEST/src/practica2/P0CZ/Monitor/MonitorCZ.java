package practica2.P0CZ.Monitor;

import java.util.concurrent.locks.ReentrantLock;

public class MonitorCZ {

    private int x = 0;
    private ReentrantLock l; 

    public MonitorCZ() {
        l = new ReentrantLock();
    }

    public void inc() {
        //Incrementa en una unitat el valor d'x
        l.lock();
        try {
            x = x + 1;
        } finally {
            l.unlock();
        }
    }

    public int getX() {
        //Ha de retornar el valor d'x
        int valor;
        l.lock();
        try {
            valor = x;
        } finally {
            l.unlock();
        }
        return valor;
    }

}
