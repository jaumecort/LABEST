package practica1.LinkedQ;

public class Node<E> {

  private E value;
  private Node<E> next;

  public Node(E e, Node<E> nxt){
    value = e;
    next = nxt;
  }

  public E getValue() {
    return value;
  }

  public void setValue(E value) {
    this.value = value;
  }

  public Node<E> getNext() {
    return next;
  }

  public void setNext(Node<E> next) {
    this.next = next;
  }

}
