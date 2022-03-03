package practica1.CircularQ;

import java.util.Iterator;
import util.Queue;

public class CircularQueue<E> implements Queue<E> {

    private final E[] queue;
    private final int N;
    private int H, n_elem;

    public CircularQueue(int N) {
        this.N = N;
        queue = (E[]) (new Object[N]);
    }

    @Override
    public int size() {
        return n_elem;
    }

    @Override
    public int free() {
        return N-n_elem;
    }

    @Override
    public boolean empty() {
        return n_elem == 0;
    }

    @Override
    public boolean full() {
        return n_elem == N;
    }

    @Override
    public E peekFirst() {
        if(empty()) return null;
        else return queue[H];
    }

    @Override
    public E get() {
        if(empty()) throw new IllegalStateException("La cola está vacía");
        else {
            E elem = queue[H];
            H = (H+1)%N;
            n_elem--;
            return elem;
        }
    }

    @Override
    public void put(E e) {
        if(full()) throw new IllegalStateException("La cola está llena");
        else {
            queue[(H+n_elem)%N] = e;
            n_elem++;
        }
    }

    @Override
    public String toString() {
        String str = new String();
        String str2 = "";
        for (int i = 0; i<n_elem; i++){
            str2 = queue[(H+i)%N].toString();
            str = str.concat(str2.concat(" "));
        }
        return str;
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator<E> {

        private int pos;

        public MyIterator(){
            pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < n_elem;
        }

        @Override
        public E next() {
            E elem = queue[(H+pos)%N];
            pos++;
            return elem;
        }

        @Override
        public void remove() {
            pos--;
            int rest = n_elem-pos;
            n_elem--;
            for(int i = 0; i<rest; i++){
                queue[(pos+i)%N] = queue[(pos+i+1)%N];
            }
        }

    }
}
