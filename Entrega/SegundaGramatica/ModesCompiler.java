import org.stringtemplate.v4.*;
import java.util.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

public class ModesCompiler extends cypherModesBaseVisitor<ST> {
  
   private STGroup templates = new STGroupFile("pythonPattern.stg");
   private String className = "";

   public String getClassName() {
        return this.className;
   }
   
   @Override public ST visitProgram(cypherModesParser.ProgramContext ctx) {
      ST res = templates.getInstanceOf("program");
      this.className = ctx.fileName().ID().getText();
      res.add("className", this.className);

     res.add("dataSizeBlock", ctx.fileName().NUM(0).getText());
     res.add("keySizeBlock", ctx.fileName().NUM(1).getText());

      if(ctx.line() != null){
         ctx.line().stream().forEach(x -> res.add("program", visit(x).render()));
      }
      if(ctx.use() != null){
        res.add("header", visit(ctx.use()).render());
      }
      String matrixClass = "";
      String pythonFile = "Standard.py";
      try {
         // for ubuntu
         matrixClass = Files.readAllLines(Paths.get(new File("../python/" + pythonFile).getAbsolutePath())).stream().reduce("", (previous, newLine) -> previous + "\n" + newLine);
      }
      catch(IOException e){
         try{
            // for windows
            matrixClass = Files.readAllLines(Paths.get(new File("..\\python\\" + pythonFile).getAbsolutePath())).stream().reduce("", (previous, newLine) -> previous + "\n" + newLine);
         }
         catch(IOException e2) {
            matrixClass = "Error getting " + pythonFile + " file";
         }
      }
      res.add("matrixClass", matrixClass);
      return res;
   }

   @Override public ST visitFileName(cypherModesParser.FileNameContext ctx) {
        return visitChildren(ctx);
   }

   @Override public ST visitInterval(cypherModesParser.IntervalContext ctx) {
        return visitChildren(ctx);
   }

   @Override public ST visitLine(cypherModesParser.LineContext ctx) {
        return visit(ctx.action());
   }

   @Override public ST visitUse(cypherModesParser.UseContext ctx) {
        ST res = templates.getInstanceOf("headerImport");
        res.add("name", ctx.ID().getText());
        return res;
   }

   @Override public ST visitActionCypherFunction(cypherModesParser.ActionCypherFunctionContext ctx) {
        return visit(ctx.cypherFunction());
   }

   @Override public ST visitActionDeclare(cypherModesParser.ActionDeclareContext ctx) {
        return visit(ctx.declare());
   }

   @Override public ST visitActionIfCond(cypherModesParser.ActionIfCondContext ctx) {
        return visit(ctx.ifCond());
   }

   @Override public ST visitActionForLoop(cypherModesParser.ActionForLoopContext ctx) {
        return visit(ctx.forLoop());
   }

   @Override public ST visitActionAssign(cypherModesParser.ActionAssignContext ctx) {
        return visit(ctx.assignment());
   }

   @Override public ST visitIfCond(cypherModesParser.IfCondContext ctx) {
        ST res = templates.getInstanceOf("if");
        res.add("arg1", visit(ctx.arg(0)).render());
        res.add("arg2", visit(ctx.arg(1)).render());
        Iterator<cypherModesParser.LineContext> it = ctx.line().iterator();
        while(it.hasNext()){
            res.add("body", visit(it.next()));
        }
        return res;
   }

   @Override public ST visitArg(cypherModesParser.ArgContext ctx) {
        ST res = templates.getInstanceOf("string");
        if(ctx.variable() != null){
            res.add("arg", visit(ctx.variable()).render() + ".value");
        }else if(ctx.NUM() != null){
            res.add("arg", ctx.NUM().getText());
        }
        return res;
   }

   @Override public ST visitForLoop(cypherModesParser.ForLoopContext ctx) {
        ST res = templates.getInstanceOf("forLoop");
        res.add("counter", ctx.ID(0).getText());
        res.add("var", ctx.ID(1).getText());
        Iterator<cypherModesParser.LineContext> it = ctx.line().iterator();
        while(it.hasNext()){
            res.add("body", visit(it.next()));
        }
      return res;
   }

   @Override public ST visitDeclare(cypherModesParser.DeclareContext ctx) {
      ST res = templates.getInstanceOf("declare");
      Iterator<TerminalNode> it = ctx.variableNames().ID().iterator();
      res.add("value", visit(ctx.value()).render());
      while(it.hasNext()){
         res.add("var", it.next().getText());
         if(it.hasNext()) {
            res.add("var", " = ");
         }
      }
      return res;
   }

   @Override public ST visitVariableNames(cypherModesParser.VariableNamesContext ctx) {
        return visitChildren(ctx);
   }

   @Override public ST visitTypes(cypherModesParser.TypesContext ctx) {
        return visitChildren(ctx);
   }

   @Override public ST visitAssignment(cypherModesParser.AssignmentContext ctx) {
        ST res = templates.getInstanceOf("assign");
        Iterator<cypherModesParser.VariableContext> it = ctx.variable().iterator();
        ST temp = visit(ctx.value());
        if(ctx.variable(0).coords() != null){
          if(!ctx.value().type.equals("function")){
               res.add("value", temp.render());
               res.add("value", ".matrix");
          }
          else {
               temp.add("adjust", ".matrix[0]");
               res.add("value", temp.render());
          }
        }
        else{
             res.add("value", temp.render());
        }
        while(it.hasNext()){
            res.add("var", visit(it.next()).render());
            if(it.hasNext()) {
                res.add("var", " = ");
            }
        }
        return res;
   }

   @Override public ST visitVariable(cypherModesParser.VariableContext ctx) {
        ST res = templates.getInstanceOf("string");

        res.add("arg", ctx.ID().getText());
        if(ctx.coords() != null){ 
            res.add("arg", ".matrix[" + visit(ctx.coords().coord(1)).render() +"]");
        }
        return res;
   }

   @Override public ST visitValueOperation(cypherModesParser.ValueOperationContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg",  visit(ctx.operation()).render());
        return res;
   }

   @Override public ST visitValueID(cypherModesParser.ValueIDContext ctx) {
      return visit(ctx.variable());
   }

   @Override public ST visitValueBYTE(cypherModesParser.ValueBYTEContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg", ctx.BYTE().getText());
        return res;
   }

   @Override public ST visitValueMatrix(cypherModesParser.ValueMatrixContext ctx) {
      return visit(ctx.matrix());
   }

   @Override public ST visitValueCypherFunction(cypherModesParser.ValueCypherFunctionContext ctx) {
      ST temp = visit(ctx.cypherFunction());
      ctx.type = ctx.cypherFunction().type;
      return temp;
   }

   @Override public ST visitValueColSize(cypherModesParser.ValueColSizeContext ctx) {
      return visit(ctx.colSize());
   }

   @Override public ST visitValueNUM(cypherModesParser.ValueNUMContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg", "Num(64, " + ctx.NUM().getText() + ")");
        return res;
   }

   @Override public ST visitColSize(cypherModesParser.ColSizeContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg", "Num(64, len(" + ctx.ID().getText() + ".matrix" + ") - 1)");
        return res;
   }

   @Override public ST visitCypherFunction(cypherModesParser.CypherFunctionContext ctx) {
          ST res = templates.getInstanceOf("cypherFunc");
          res.add("className", ctx.ID().getText());
          res.add("data", visit(ctx.functionArgs()).render());
          ctx.type = "function";
          return res;
     }

     @Override public ST visitFunctionArgs(cypherModesParser.FunctionArgsContext ctx) {
          ST res = templates.getInstanceOf("string");
          res.add("arg", ctx.ID().getText());
          if(ctx.coords() != null){
               res.add("arg", ".get_row(" + visit(ctx.coords()).render().split(",")[1].replace(" ", "") + ")");
          }
          return res;
     }
     
   @Override public ST visitOperByte(cypherModesParser.OperByteContext ctx) {
    ST res = templates.getInstanceOf("string");
    res.add("arg", ctx.BYTE().getText());
    return res;
   }

   @Override public ST visitOperID(cypherModesParser.OperIDContext ctx) {
          ST res = templates.getInstanceOf("string");

          res.add("arg", ctx.variable().ID().getText());
          if(ctx.variable().coords() != null){ 
               res.add("arg", ".get_row(" + visit(ctx.variable().coords().coord(1)).render() +")");
          }
          return res; 
   }

   @Override public ST visitOperXor(cypherModesParser.OperXorContext ctx) {
        ST res = templates.getInstanceOf("string");        
        res.add("arg",visit(ctx.operation(0)).render() + "." + "operation_xor" + "(" + visit(ctx.operation(1)).render() + ")");
        return res;
   }

   @Override public ST visitOperParentheses(cypherModesParser.OperParenthesesContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg", "( " + visit(ctx.operation()).render() + " )");
        return res;
   }

   @Override public ST visitOperPlus(cypherModesParser.OperPlusContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg",visit(ctx.operation(0)).render() + "." + "add" + "(" + visit(ctx.operation(1)).render() + ")");
        return res;
   }

   @Override public ST visitOperNum(cypherModesParser.OperNumContext ctx) {
        ST res = templates.getInstanceOf("string");
        res.add("arg", ctx.NUM().getText());
        return res;
   }

   @Override public ST visitMatrix(cypherModesParser.MatrixContext ctx) {
        ST res = templates.getInstanceOf("string");
        Iterator<cypherModesParser.LineMatrixContext> it = ctx.lineMatrix().iterator();
        int type = 0; 
        String arg = "";
        boolean firstDone = false;
        while(it.hasNext()){
            arg = visit(it.next()).render();
            if(!firstDone){
                switch(ctx.lineMatrix(0).type){
                    case "num":
                        type = 64;
                        break;
                    case "byte":
                        type = 8;
                        break;
                    case "bit":
                        type = 0;
                        break;
                }
                res.add("arg", "Matrix(" + type + ", [");
                firstDone = true;
            }
            res.add("arg", "[" + arg + "]");
            if(it.hasNext()) {
                res.add("arg", ", ");
            }
        }
        res.add("arg", "])");    
        return res;
   }

   @Override public ST visitLineMatrix(cypherModesParser.LineMatrixContext ctx) {
        ST res = templates.getInstanceOf("string");
        Iterator<TerminalNode> itNUM = ctx.NUM().iterator();
        Iterator<TerminalNode> itBYTE = ctx.BYTE().iterator();
        Iterator<TerminalNode> it = itNUM;
        ctx.type = "num";
        if(itBYTE.hasNext()) {
            it = itBYTE;
            ctx.type = "byte"; 
        }
        while(it.hasNext()){
            res.add("arg", it.next().getText());
            if(it.hasNext()){
                res.add("arg", ", ");
            }
        }
        return res;
   }

   @Override public ST visitCoords(cypherModesParser.CoordsContext ctx) {
        ST res = templates.getInstanceOf("coordenates");
        res.add("x",visit(ctx.coord(0)).render());
        res.add("y",visit(ctx.coord(1)).render());
        return res;
   }

   @Override public ST visitCoord(cypherModesParser.CoordContext ctx) {
          ST res = templates.getInstanceOf("string");
          if(ctx.NUM() != null){
               res.add("arg", ctx.NUM().getText());
          }else if(ctx.operation() != null){
               res.add("arg", visit(ctx.operation()).render() + ".value");
          }else if(ctx.ID() != null){
               res.add("arg", ctx.ID().getText() + ".value");
          }
        return res;
    }
}
