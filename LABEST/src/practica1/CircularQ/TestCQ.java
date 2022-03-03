    package practica1.CircularQ;

//import java.util.Arrays;

//import javax.xml.catalog.Catalog;

public class TestCQ {

  public static void main(String[] args) {
    CircularQueue<Integer> q = new CircularQueue<>(5);
    

    //Método put()
    q.put(1);
    q.put(2);
    q.put(3);
    q.put(4);
    q.put(5);
    try {
      q.put(6);
    } catch (IllegalStateException e) {
      System.out.println("Metodo put() correcto.");
    }

    //Metodo size()
    if (q.size() == 5) System.out.println("Metodo size() correcto");
    else System.out.println("Metodo size() incorrecto");

    //Metodo full()
    if (q.full()) System.out.println("Metodo full() correcto");
    else System.out.println("Metodo full() incorrecto");

    //metodo get()
    q.get();
    q.get();
    q.get();
    q.put(6);
    q.put(7);
    q.put(8);
    q.get();
    q.get();
    q.get();
    q.get();
    q.get();

    try {
      q.get();
    } catch (IllegalStateException e) {
      System.out.println("Metodo get() correcto.");
    }

    //Metodo free()
    if (q.free() == 5) System.out.println("Metodo free() correcto.");
    else System.out.println("Metodo free() incorrecto.");
    q.put(7);
    q.put(8);
    if (q.free() == 3) System.out.println("Metodo free() correcto.");
    else System.out.println("Metodo free() incorrecto.");
    q.put(7);
    q.put(8);
    q.put(8);
    if (q.free() == 0) System.out.println("Metodo free() correcto.");
    else System.out.println("Metodo free() incorrecto.");
    q.get();
    q.get();
    q.get();
    q.get();
    q.get();

    //Metodo empty()
    if(q.empty()) System.out.println("Metodo empty() correcto.");
    else System.out.println("Metodo empty() incorrecto.");

    //Metodo peekfirst()
    q.put(5);
    if (q.peekFirst() == q.get()){
      if (q.peekFirst() == null) System.out.println("Metodo peekFirst() correcto.");
      else System.out.println("Metodo peekFirst() incorrecto.");
    }
    else System.out.println("Metodo peekFirst() incorrecto.");

    //Metodo toString()
    q.put(1);
    q.put(2);
    q.put(3);
    q.put(4);
    q.put(5);
    
    String comp = "1\n2\n3\n4\n5\n";
    if (q.toString().equals(comp)) System.out.println("Metodo toString() correcto.");
    else System.out.println("Metodo toString() incorrecto.");
  }
  
}
