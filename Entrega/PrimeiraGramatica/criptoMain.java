import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;
import java.io.FileWriter;

public class criptoMain {
   public static void main(String[] args) {
      try {
         // create a CharStream that reads from standard input:
         CharStream input = CharStreams.fromStream(System.in);
         // create a lexer that feeds off of input CharStream:
         criptoLexer lexer = new criptoLexer(input);
         // create a buffer of tokens pulled from the lexer:
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         // create a parser that feeds off the tokens buffer:
         criptoParser parser = new criptoParser(tokens);
         // replace error listener:
         //parser.removeErrorListeners(); // remove ConsoleErrorListener
         //parser.addErrorListener(new ErrorHandlingListener());
         // begin parsing at program rule:
         ParseTree tree = parser.program();
         if (parser.getNumberOfSyntaxErrors() == 0) {
            // print LISP-style tree:
            // System.out.println(tree.toStringTree(parser));
            CriptoSemantica1 visitor0 = new CriptoSemantica1();
            if(!visitor0.visit(tree)){
               System.exit(1);
            }
            CriptoCompiler compiler = new CriptoCompiler();
            ST result = compiler.visit(tree);
            try{
               String file_content = result.render();
               String file_name = compiler.getClassName() + ".py";
               FileWriter writer = new FileWriter(file_name);
               writer.write(file_content);
               writer.close();
               System.out.println("Success writing to " + file_name);
            }
            catch(IOException e) {
               System.out.println("Error writing to file");
            }
         }
      }
      catch(IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
      catch(RecognitionException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }
}
