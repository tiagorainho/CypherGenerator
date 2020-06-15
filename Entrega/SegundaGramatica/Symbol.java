import static java.lang.System.*;
import java.util.Scanner;

public class Symbol
{
   public Symbol(String name, String type) {
      assert name != null;
      assert type != null;

      this.name = name;
      this.type = type;
   }
   public Symbol(String name, String type,int line,int col) {
      assert name != null;
      assert type != null;


      this.name = name;
      this.type = type;
   }
   public String getName(){
      return name;
   }

   public String getType(){
      return type;
   }
   public boolean isMatrix()
   {
      if(this.type.contains("-M")){
         return true;
      }
      return false;
   }

   @Override
   public String toString() {
      return "Name: " + name + ",Type: " + type;
   }

   protected final String name;
   protected final String type;
}

