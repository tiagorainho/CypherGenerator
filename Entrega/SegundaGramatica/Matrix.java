import static java.lang.System.*;
import java.util.Scanner;

public class Matrix
{
   public Matrix(String values,int lines,int cols,String type){
      this.values = values;
      this.cols = cols;
      this.lines = lines;
      this.type = type;
   }
   /**
    * @return the cols
    */
   public int getCols() {
      return cols;
   }

   /**
    * @return the lines
    */
   public int getLines() {
      return lines;
   }

   /**
    * @return the values
    */
   public String getValues() {
      return values;
   }

   public boolean isSameSize(Matrix m){
      if((m.getLines() == lines) && (m.getCols() == cols)){
         return true;
      }
      return false;
   }

   @Override
   public String toString() {
      return "Matrix: " + values + "\nLines: " + lines + "\nCols: " + cols + "\nType: " + type;
   }
   
   private String values;
   private int lines;
   private int cols;
   private String type;
}

