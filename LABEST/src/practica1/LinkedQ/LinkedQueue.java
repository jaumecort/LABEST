package practica1.LinkedQ;

import java.util.Iterator;
import util.Queue;

public class LinkedQueue<E> implements Queue<E> {

  private Node<E> primer, ultim;
  private int n_elem;

  public LinkedQueue() {
    primer = null;
    ultim = null;
    n_elem = 0;
  }

  @Override
  public int size() {
    return n_elem;
  }

  @Override
  public int free() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean empty() {
    return n_elem == 0;
  }

  @Override
  public boolean full() {
    return false;
  }

  @Override
  public E peekFirst() {
    if (empty())
      return null;
    return primer.getValue();
  }

  @Override
  public E get() {
    if (empty())
      throw new IllegalStateException("La cola esta vacía");
    E tmp = primer.getValue();
    primer = primer.getNext();
    n_elem--;
    return tmp;
  }

  @Override
  public void put(E e) {
    Node<E> node = new Node<>(e, null);
    if (empty())
      primer = node;
    else
      ultim.setNext(node);
    ultim = node;
    n_elem++;
  }

  @Override
  public String toString() {
    String str = "";
    Node<E> actual = primer;
    // if(empty()) throw new IllegalStateException("cola vacia");
    while (actual != null) {
      str = str.concat(actual.getValue().toString()).concat("\n");
      actual = actual.getNext();
    }
    return str;
  }

  @Override
  public Iterator<E> iterator() {
    return new MyIterator();
  }

  class MyIterator implements Iterator {

    Node<E> actual;
    Node<E> anterior;
    Node<E> preanterior;
    boolean next_cridat;

    public MyIterator() {
      actual = null;
      anterior = null;
      preanterior = null;
      next_cridat = false;
    }

    @Override
    public boolean hasNext() {
      if (empty())
        return false;
      if (actual == anterior && anterior == null)
        return true;
      return actual.getNext() != null;
    }

    @Override
    public E next() {
      if (actual == anterior && anterior == preanterior && anterior == null) {
        actual = primer;
        next_cridat = true;
        return actual.getValue();
      }

      preanterior = anterior;
      anterior = actual;
      actual = actual.getNext();
      next_cridat = true;
      return actual.getValue();
    }

    @Override
    public void remove() {
      if (next_cridat == true){
        if(!this.hasNext()){
          ultim = anterior;
        }
        if (anterior == null) {
          primer = actual.getNext();
          actual = null;
        }
        else {
          anterior.setNext(actual.getNext());
          actual = anterior;
          anterior = preanterior;
        }
        n_elem--;
        next_cridat = false;
      } else throw new IllegalStateException("No hem cridat next");
    }

  }
}
