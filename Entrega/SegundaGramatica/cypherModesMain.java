import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;
import java.io.FileWriter;

public class cypherModesMain {
   public static void main(String[] args) {
      try {
         // create a CharStream that reads from standard input:
         CharStream input = CharStreams.fromStream(System.in);
         // create a lexer that feeds off of input CharStream:
         cypherModesLexer lexer = new cypherModesLexer(input);
         // create a buffer of tokens pulled from the lexer:
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         // create a parser that feeds off the tokens buffer:
         cypherModesParser parser = new cypherModesParser(tokens);
         // replace error listener:
         //parser.removeErrorListeners(); // remove ConsoleErrorListener
         //parser.addErrorListener(new ErrorHandlingListener());
         // begin parsing at program rule:
         ParseTree tree = parser.program();
         if (parser.getNumberOfSyntaxErrors() == 0) {
            // print LISP-style tree:
            // System.out.println(tree.toStringTree(parser));
            Semantic2Check visitor0 = new Semantic2Check();
            if(!visitor0.visit(tree)){
               System.exit(1);
            }
            ModesCompiler compiler = new ModesCompiler();
            ST result = compiler.visit(tree);
            try{
               String file_name = compiler.getClassName() + ".py";
               FileWriter writer = new FileWriter(file_name);
               writer.write(result.render());
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
