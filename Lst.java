import java.util.ArrayList;

public class Lst {

   private ArrayList<Fob> list;

   public Lst() {
      list = new ArrayList<Fob>();
   }

   public Num size() {
      return new Num( list.size() );
   }
}