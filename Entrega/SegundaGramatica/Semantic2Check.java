import java.util.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.io.*;

public class Semantic2Check extends cypherModesBaseVisitor<Boolean> {

   protected Map<String,Symbol> symbolTable = new HashMap<>();

   public Map<String,Symbol> symbolTable()
   {
      return symbolTable;
   }

   @Override public Boolean visitProgram(cypherModesParser.ProgramContext ctx) {
      Boolean res = true;
      if(!visit(ctx.fileName())){
         res = false;
      }
      if(!visit(ctx.use())){
         res =  false;
      }
      Iterator<cypherModesParser.LineContext> iterator1 = ctx.line().iterator();
      while(iterator1.hasNext())
      {  
         if(!visit(iterator1.next())){
            res = false;
         }
      }
      ErrorHandling.printInfo("Numero de erros: " + ErrorHandling.errorCount());
      return res;
   }

   @Override public Boolean visitFileName(cypherModesParser.FileNameContext ctx) {
      symbolTable().put("data",new Symbol("data","byte-M"));
      symbolTable().put("out",new Symbol("out","byte-M"));
      return true;
   }

   @Override public Boolean visitInterval(cypherModesParser.IntervalContext ctx) {
      ctx.size = new int[2];
      ctx.size[0] = Integer.parseInt(ctx.NUM(0).getText());
      ctx.size[1] = Integer.parseInt(ctx.NUM(1).getText());
      return true;
   }

   @Override public Boolean visitLine(cypherModesParser.LineContext ctx) {
      boolean res = visit(ctx.action());
      return res;
   }

   @Override public Boolean visitUse(cypherModesParser.UseContext ctx) {
      File f = new File(ctx.ID().getText() + ".py");
      if(f.exists() && !f.isDirectory()) { 
         symbolTable().put(ctx.ID().getText(),new Symbol(ctx.ID().getText(),"func"));
         return true;
      }
      return false;
   }

   @Override public Boolean visitActionDeclare(cypherModesParser.ActionDeclareContext ctx) {
      boolean res = visit(ctx.declare());
      return res;
   }

   @Override public Boolean visitActionForLoop(cypherModesParser.ActionForLoopContext ctx) {
      boolean res = visit(ctx.forLoop());
      return true;
   }

   @Override public Boolean visitActionAssign(cypherModesParser.ActionAssignContext ctx) {
      boolean res = visit(ctx.assignment());
      return res;
   }

   @Override public Boolean visitForLoop(cypherModesParser.ForLoopContext ctx) {
      Boolean res = true;
      if(symbolTable().containsKey(ctx.ID(0).getText())){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " already exists");
         ErrorHandling.errorCount();
         res =  false;
      }
      symbolTable().put(ctx.ID(0).getText(),new Symbol(ctx.ID(0).getText(),"num"));
      if(!symbolTable().containsKey(ctx.ID(1).getText())){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
         ErrorHandling.errorCount();
         res =  false;
      }
      if(!symbolTable().get(ctx.ID(1).getText()).isMatrix()){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " is not a matrix");
         ErrorHandling.errorCount();
         res =  false;
      }
      Iterator<cypherModesParser.LineContext> iterator1 = ctx.line().iterator();
      while(iterator1.hasNext())
      {  
         if(!visit(iterator1.next())){
            res = false;
         }
      }
      symbolTable().remove(ctx.ID(0).getText());
      return true;
   }

   @Override public Boolean visitValueCypherFunction(cypherModesParser.ValueCypherFunctionContext ctx) {
      Boolean res = visit(ctx.cypherFunction());
      ctx.type = ctx.cypherFunction().type;
      return res;
   }

   @Override public Boolean visitValueColSize(cypherModesParser.ValueColSizeContext ctx) {
      if(!visit(ctx.colSize())){
         return false;
      }
      ctx.type = ctx.colSize().type;
      return true;
   }

   @Override public Boolean visitDeclare(cypherModesParser.DeclareContext ctx) {
      Iterator<TerminalNode> iterator = ctx.variableNames().ID().iterator();
      while(iterator.hasNext())
      {
         String varName = iterator.next().getText();
         if(symbolTable().containsKey(varName))
         {  
            ErrorHandling.printError(ctx,"Declared variable already exists");
            ErrorHandling.errorCount();
            return false;
         }
         Symbol temp;
         if(visit(ctx.value())){
            if(ctx.interval() != null){
               visit(ctx.interval());
               if(ctx.value().size != null){
                  if((ctx.interval().size[0] != ctx.value().size[0]) || (ctx.interval().size[1] != ctx.value().size[1])){
                     ErrorHandling.printError(ctx,"Matrix dimensions are incorrect");
                     ErrorHandling.errorCount();
                     return false;
                  }
               }
               if(!ctx.value().type.equals(ctx.types().getText()+"-M") && !ctx.value().type.equals("all")){
                  ErrorHandling.printError(ctx,"Variables: have diferent types " + ctx.value().type + " and " +  ctx.types().getText());
                  ErrorHandling.errorCount();
                  return false;
               }
               temp = new Symbol(varName,ctx.types().getText()+"-M");
            }else{
               if(ctx.value().type.contains("-M")){
                  ErrorHandling.printError(ctx,"Variable: is not a matrix");
                  ErrorHandling.errorCount();
                  return false;
               }
               if(!ctx.value().type.equals(ctx.types().getText()) && !ctx.value().type.equals("all")){
                  ErrorHandling.printError(ctx,"Variables: have diferent types " + ctx.value().type + " and " +  ctx.types().getText());
                  ErrorHandling.errorCount();
                  return false;
               }
               temp = new Symbol(varName,ctx.types().getText());
            }
            symbolTable().put(varName,temp);    
            
         }
      }
      return true;
   }

   @Override public Boolean visitVariableNames(cypherModesParser.VariableNamesContext ctx) {
      return true;
   }

   @Override public Boolean visitTypes(cypherModesParser.TypesContext ctx) {
      return true;
   }

   @Override public Boolean visitActionCypherFunction(cypherModesParser.ActionCypherFunctionContext ctx) {
      return visit(ctx.cypherFunction());
   }

   @Override public Boolean visitCypherFunction(cypherModesParser.CypherFunctionContext ctx) {
      if(!symbolTable().containsKey(ctx.ID().getText())){
         ErrorHandling.printError(ctx," Function " + ctx.ID().getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      if(!symbolTable().get(ctx.ID().getText()).getType().equals("func")){
         ErrorHandling.printError(ctx,"Use Function " + ctx.ID().getText() + " in not beeing used");
         ErrorHandling.errorCount();
         return false;
      }
      if(!visit(ctx.functionArgs())){
         return false;
      }
      if(!(ctx.functionArgs().type.equals("byte-M") || ctx.functionArgs().type.equals("all"))){
         ErrorHandling.printError(ctx,"Use Function argument" + ctx.functionArgs().ID().getText() + " in not a matrix");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = "byte-M";
      return true;
   }

   @Override public Boolean visitCoords(cypherModesParser.CoordsContext ctx) {
      if(visit(ctx.coord(0)) && visit(ctx.coord(1))){
         return true;
      }
      return false;
   }

   @Override public Boolean visitCoord(cypherModesParser.CoordContext ctx) {
      if(ctx.ID() != null){      
            if(!symbolTable().containsKey(ctx.ID().getText())){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!symbolTable().get(ctx.ID()).getType().equals("num")){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID() + " is not a Num");
               ErrorHandling.errorCount();
               return false;
            }
      }
      if(ctx.operation() != null)
      {
         if(!visit(ctx.operation())){
            return false;
         }
         if(!ctx.operation().type.equals("num")){
            return false;
         }
      }
      return true;
   }

   @Override public Boolean visitAssignment(cypherModesParser.AssignmentContext ctx) {
      boolean res = false;
      Iterator<cypherModesParser.VariableContext> iterator = ctx.variable().iterator();
      while(iterator.hasNext())
      {
         cypherModesParser.VariableContext temp = iterator.next();
         if(!visit(temp)){
            return false;
         }
         String type = temp.type;

         res = visit(ctx.value());
         if(res == false)
         {
            return res;
         }
         if(ctx.value().type.equals(type) || ctx.value().type.equals("all") || type.equals("all"))
         {                                                                                                                                                        
         
         }
         else{
            ErrorHandling.printError(ctx,"Variables: have diferent types ");
            ErrorHandling.errorCount();
            return res;
         }
      }
      res = true;
      return res;
   }

   @Override public Boolean visitVariable(cypherModesParser.VariableContext ctx) {
      boolean res = false;
      if(symbolTable().containsKey(ctx.ID().getText()))
      {
         Symbol temp = symbolTable().get(ctx.ID().getText());
         ctx.type = temp.getType();
         res = true;
      }
      else
      {
         ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
         ErrorHandling.errorCount();
      }
      if(ctx.coords() != null){
         Symbol temp = symbolTable().get(ctx.ID().getText());
         if(symbolTable().get(ctx.ID().getText()).isMatrix()){
            res =  true;
            ctx.type = "all"; 
         }else{
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "is not a matrix");
            ErrorHandling.errorCount();
         }
         if(visit(ctx.coords())){
            res = true;
         }  
      }
      return res;
   }

   @Override public Boolean visitFunctionArgs(cypherModesParser.FunctionArgsContext ctx) {
      boolean res = false;
      if(symbolTable().containsKey(ctx.ID().getText()))
      {
         Symbol temp = symbolTable().get(ctx.ID().getText());
         ctx.type = temp.getType();
         res = true;
      }
      else
      {
         ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
         ErrorHandling.errorCount();
      }
      if(ctx.coords() != null){
         Symbol temp = symbolTable().get(ctx.ID().getText());
         if(symbolTable().get(ctx.ID().getText()).isMatrix()){
            res =  true;
            ctx.type = "all"; 
         }else{
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "is not a matrix");
            ErrorHandling.errorCount();
         }
         if(visit(ctx.coords())){
            res = true;
         }  
      }
      return res;
   }

   @Override public Boolean visitValueNUM(cypherModesParser.ValueNUMContext ctx) {
      ctx.type = "num";
      return true;
   }

   @Override public Boolean visitValueID(cypherModesParser.ValueIDContext ctx) {
      boolean res = false;
      if(visit(ctx.variable())){
         res = true;
      }
      ctx.type = ctx.variable().type;
      return res;
   }

   @Override public Boolean visitValueBYTE(cypherModesParser.ValueBYTEContext ctx) {
      ctx.type = "byte";
      return true;
   }

   @Override public Boolean visitColSize(cypherModesParser.ColSizeContext ctx) {
      if(!symbolTable().containsKey(ctx.ID().getText()))
      {
         ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
         ErrorHandling.errorCount();
      }
      if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
         ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "is not a matrix");
         ErrorHandling.errorCount();
      }
      ctx.type = "num";
      return true;
   }

   @Override public Boolean visitValueMatrix(cypherModesParser.ValueMatrixContext ctx) {
      if(visit(ctx.matrix())){
         ctx.type = ctx.matrix().type;
         ctx.size = ctx.matrix().size;
         return true;
      }
      return false;
   }

   @Override public Boolean visitValueOperation(cypherModesParser.ValueOperationContext ctx) {
      boolean res = false;
      if(visit(ctx.operation())){
         res = true;
      }
      ctx.type = ctx.operation().type;
      return res;
   }

   @Override public Boolean visitOperByte(cypherModesParser.OperByteContext ctx) {
      ctx.type = "byte";
      return true;
   }

   @Override public Boolean visitOperID(cypherModesParser.OperIDContext ctx) {
      boolean res = false;
      if(visit(ctx.variable())){
         res = true;
      }
      ctx.type = ctx.variable().type;
      return res;
   }

   @Override public Boolean visitOperXor(cypherModesParser.OperXorContext ctx) {
      if(!visit(ctx.operation(0)) || !visit(ctx.operation(1))){
         return false;
      } 
      if(!(ctx.operation(0).type.equals(ctx.operation(1).type) || ctx.operation(0).type.equals("all"))){
         ErrorHandling.printError(ctx,"Operators are not the same type");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = ctx.operation(0).type;
      return true;
   }

   @Override public Boolean visitOperPlus(cypherModesParser.OperPlusContext ctx) {
      if(!visit(ctx.operation(0)) || !visit(ctx.operation(1))){
         return false;
      } 
      if(!ctx.operation(0).type.equals(ctx.operation(1).type)){
         ErrorHandling.printError(ctx,"Operators are not the same type");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = ctx.operation(0).type;
      return true;
   }

   @Override public Boolean visitOperNum(cypherModesParser.OperNumContext ctx) {
      ctx.type = "num";
      return true;
   }

   @Override public Boolean visitMatrix(cypherModesParser.MatrixContext ctx) {
      List<cypherModesParser.LineMatrixContext> list = ctx.lineMatrix();
      ctx.size = new int[2];
      ctx.type = null;
      ctx.size[0] = list.size();
      for(int i=0;i<list.size();i++){
         visit(ctx.lineMatrix(i));
         if(ctx.type == null){
            ctx.type = list.get(i).type;
            ctx.size[1] = list.get(i).c;
         }
         if(ctx.size[1] != list.get(i).c){
            
            ErrorHandling.printError(ctx,"Iconsistent columm size");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.type != list.get(i).type){
           
            ErrorHandling.printError(ctx,"The values in the matrix must all be of the same type");
            ErrorHandling.errorCount();
            
            return false;
         }
      }
      ctx.type = ctx.type+"-M";
      return true;
   }

   @Override public Boolean visitLineMatrix(cypherModesParser.LineMatrixContext ctx) {
      List<TerminalNode> t = null;
      int c = 0;
      if(ctx.NUM().size() != 0){
         t = ctx.NUM();
         ctx.type = "num";
      }
      if(ctx.BYTE().size() != 0){
         t = ctx.BYTE();
         ctx.type = "byte";
      }
      ctx.c = t.size();
      return true;
   }

   @Override public Boolean visitIfCond(cypherModesParser.IfCondContext ctx)
   {
      if(visit(ctx.arg(0)) && visit(ctx.arg(1))){
         return true;
      }
      return false;
   }

   @Override public Boolean visitArg(cypherModesParser.ArgContext ctx){
      if(!visit(ctx.variable())){
         return false;
      }
      if(!ctx.variable().type.equals("num")){
         ErrorHandling.printError(ctx,"Variable: " +ctx.variable().ID().getText() + " is not a Num");
         ErrorHandling.errorCount();
         return false;
      }
      return true;
   }
   
}


