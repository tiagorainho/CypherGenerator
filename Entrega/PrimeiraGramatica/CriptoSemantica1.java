import java.util.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.io.*;

public class CriptoSemantica1 extends criptoBaseVisitor<Boolean> {
   protected Map<String,Symbol> symbolTable = new HashMap<>();
   protected Stack<Map<String,Symbol>> stack = new Stack<>();
   protected HashMap<String,Functions> functionCreated = new HashMap<>();

   public Map<String,Symbol> symbolTable()
   {
      return stack.peek();
   }

   @Override public Boolean visitProgram(criptoParser.ProgramContext ctx){
      Boolean res = visit(ctx.init());
      ErrorHandling.printInfo("Error count: " + ErrorHandling.errorCount());
      return res;
   }
   
   @Override public Boolean visitInit(criptoParser.InitContext ctx){
      stack.push(symbolTable);
      Boolean res = true;
      if(!visit(ctx.fileName())){
         res = false;
      }
      Iterator<criptoParser.Import1Context> iterator0 = ctx.import1().iterator();
      while(iterator0.hasNext())
      {  
         if(!visit(iterator0.next())){
            res = false;
         }
      }
      Iterator<criptoParser.FunctionContext> iterator2 = ctx.function().iterator();
      while(iterator2.hasNext())
      {
         if(!visit(iterator2.next())){
            res = false;
         }
      }
      Iterator<criptoParser.LineContext> iterator1 = ctx.line().iterator();
      while(iterator1.hasNext())
      {  
         if(!visit(iterator1.next())){
            res = false;
         }
      }
      return res;
   }

   @Override public Boolean visitFileName(criptoParser.FileNameContext ctx) {
         symbolTable().put("data",new Symbol("data","byte-M"));
         symbolTable().put("key",new Symbol("key","byte-M"));
         return true;
   }

   @Override public Boolean visitImport1(criptoParser.Import1Context ctx) {
      File f = new File(ctx.ID().getText() + ".py");
      if(f.exists() && !f.isDirectory()) { 
         Functions temp = new Functions(ctx.ID().getText(),"byte-M");
         temp.addType("byte-M");
         temp.addType("byte-M");
         functionCreated.put(ctx.ID().getText(),temp);
         symbolTable().put(ctx.ID().getText(),new Symbol(f.getName(),"func"));
         return true;
      }
      ErrorHandling.printError(ctx,"File " + ctx.ID().getText() + ".py does not exist");
      ErrorHandling.errorCount();
      return false;
   }

   @Override public Boolean visitLine(criptoParser.LineContext ctx) {
      Boolean res = visit(ctx.action());
      return res;
   }

   @Override public Boolean visitActionDeclare(criptoParser.ActionDeclareContext ctx) {
      Boolean res = visit(ctx.declare());
      return res;
   }

   @Override public Boolean visitActionForLoop(criptoParser.ActionForLoopContext ctx) {
      return visit(ctx.forLoop());
   }

   @Override public Boolean visitActionIfCond(criptoParser.ActionIfCondContext ctx) {
      return visit(ctx.ifCond());
   }

   @Override public Boolean visitActionWhileCond(criptoParser.ActionWhileCondContext ctx) {
      return visit(ctx.whileLoop());
   }

   @Override public Boolean visitActionCommands(criptoParser.ActionCommandsContext ctx) {
      return visit(ctx.commands());
   }

   @Override public Boolean visitActionAssign(criptoParser.ActionAssignContext ctx) {
      return visit(ctx.assignment());
   }

   @Override public Boolean visitInterval(criptoParser.IntervalContext ctx) {
      ctx.size = new int[2];
      ctx.size[0] = Integer.parseInt(ctx.NUM(0).getText());
      ctx.size[1] = Integer.parseInt(ctx.NUM(1).getText());
      return true;
   }

   @Override public Boolean visitBlockCode(criptoParser.BlockCodeContext ctx) {
      Iterator<criptoParser.LineContext> iterator0 = ctx.line().iterator();
      Boolean res = true;
      while(iterator0.hasNext())
      {  
         if(!visit(iterator0.next())){
            res =  false;
         }
      }
      return res;
   }

   @Override public Boolean visitDeclare(criptoParser.DeclareContext ctx) {
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

   @Override public Boolean visitAssignment(criptoParser.AssignmentContext ctx) {
      boolean res = false;
      Iterator<TerminalNode> iterator = ctx.variableNames().ID().iterator();
      while(iterator.hasNext())
      {
         String varName = iterator.next().getText();
         if(!symbolTable().containsKey(varName))
         {
            
            ErrorHandling.printError(ctx,"Variable: " + varName + " " + "as not been declared");
            ErrorHandling.errorCount();
            return res;
         }
         String type = symbolTable().get(varName).getType();

         res = visit(ctx.value());
         if(res == false)
         {
            return res;
         }
         if(ctx.value().type.equals(type) || ctx.value().type.equals("all"))
         {                                                                                                                                                        
         
         }
         else{
            
            ErrorHandling.printError(ctx,"Variables: have diferent types ");
            ErrorHandling.errorCount();
            return res;
         }
      }  
      return true;
   }

   @Override public Boolean visitValueMatrix(criptoParser.ValueMatrixContext ctx) {
      if(visit(ctx.matrix())){
         ctx.type = ctx.matrix().type;
         ctx.size = ctx.matrix().size;
         return true;
      }
      return false;
   }

   @Override public Boolean visitValueOperators(criptoParser.ValueOperatorsContext ctx) {
      if(!visit(ctx.operators())){
         return false;
      }
      ctx.type = ctx.operators().type;
      return true;
   }

   @Override public Boolean visitValuePredefined_Matrix(criptoParser.ValuePredefined_MatrixContext ctx) {
      if(!visit(ctx.predefined_Matrix())){
         return false;
      }
      ctx.type = ctx.predefined_Matrix().type;
      return true;
   }

   @Override public Boolean visitValueGetCommands(criptoParser.ValueGetCommandsContext ctx) {
      if(visit(ctx.getCommands())){
         ctx.type = ctx.getCommands().type;
         return true;
      }
      return false;
   }

   @Override public Boolean visitValueBIT(criptoParser.ValueBITContext ctx) {
      ctx.type = "bit";
      return true;
   }

   @Override public Boolean visitValueNUM(criptoParser.ValueNUMContext ctx) {
      ctx.type = "num";
      return true;
   }

   @Override public Boolean visitValueID(criptoParser.ValueIDContext ctx) {
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
      return res;
   }

   @Override public Boolean visitValueBYTE(criptoParser.ValueBYTEContext ctx) {
      ctx.type = "byte";
      return true;
   }

   @Override public Boolean visitVariableNames(criptoParser.VariableNamesContext ctx) {
      return true;
   }

   @Override public Boolean visitMatrix(criptoParser.MatrixContext ctx) {
      List<criptoParser.LineMatrixContext> list = ctx.lineMatrix();
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

   @Override public Boolean visitLineMatrix(criptoParser.LineMatrixContext ctx) {
      List<TerminalNode> t = null;
      int c = 0;
      if(ctx.BIT().size() != 0){
         t = ctx.BIT();
         ctx.type = "bit";
      }
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

   @Override public Boolean visitFunctionTypes(criptoParser.FunctionTypesContext ctx) {
      return true;
   }
   
   @Override public Boolean visitFunction(criptoParser.FunctionContext ctx) {
      if(symbolTable().containsKey(ctx.ID(0).getText())){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() +" already exists");
         ErrorHandling.errorCount();
         return false;
      }
      if(ctx.getText().contains("nothing"))
      {
         Functions f = new Functions(ctx.ID(0).getText(),"nothing");
         List<criptoParser.FunctionTypesContext> list = ctx.functionTypes();
         Iterator<TerminalNode> iterator = ctx.ID().iterator();
         Map<String,Symbol> newStack = new HashMap<>();
         
         for (Map.Entry<String,Functions> entry : functionCreated.entrySet())
            if(symbolTable().containsKey(entry.getKey()))
            {
               newStack.put(entry.getKey(),symbolTable().get(entry.getKey()));
            }
         int i = 0;
         String nothing = iterator.next().getText();
         while(iterator.hasNext())
         {
            String name = iterator.next().getText();
            if(newStack.containsKey(name))
            {
               ErrorHandling.printError(ctx,"Name " +name +" as already been declared as a function");
               ErrorHandling.errorCount();
               return false;
            }
            newStack.put(name,new Symbol(name,list.get(i).getText()));
            f.addType(list.get(i).getText());
            i++;
         }
         functionCreated.put(ctx.ID(0).getText(),f);
         stack.push(newStack);
         if(!visit(ctx.blockCode())){
            return false;
         }
         stack.pop();
         symbolTable().put(ctx.ID(0).getText(),new Symbol(ctx.ID(0).getText(),"func"));
         return true;
      }
      else
      {
         Functions f = new Functions(ctx.ID(0).getText(),ctx.functionTypes(0).getText());
         List<criptoParser.FunctionTypesContext> list = ctx.functionTypes();
         Iterator<TerminalNode> iterator = ctx.ID().iterator();
         Map<String,Symbol> newStack = new HashMap<>();
         
         for (Map.Entry<String,Functions> entry : functionCreated.entrySet())
            if(symbolTable().containsKey(entry.getKey()))
            {
               newStack.put(entry.getKey(),symbolTable().get(entry.getKey()));
            }
         int i = 1;
         String nothing = iterator.next().getText();
         while(iterator.hasNext())
         {
            String name = iterator.next().getText();
            if(newStack.containsKey(name))
            {
               ErrorHandling.printError(ctx,"Name " +name +" as already been declared as a function");
               ErrorHandling.errorCount();
               return false;
            }
            newStack.put(name,new Symbol(name,list.get(i).getText()));
            f.addType(list.get(i).getText());
            i++;
         }
         functionCreated.put(ctx.ID(0).getText(),f);
         
         stack.push(newStack);
         if(!visit(ctx.blockCode())){
            return false;
         }
         stack.pop();
         symbolTable().put(ctx.ID(0).getText(),new Symbol(ctx.ID(0).getText(),"func"));
      }
      return true;
   }

   @Override public Boolean visitTypes(criptoParser.TypesContext ctx) {
      return true;
   }

   @Override public Boolean visitGetValueCommands(criptoParser.GetValueCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         ctx.type = symbolTable().get(ctx.ID().getText()).getType().split("-M")[0];
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         ctx.type = ctx.getCommands().type.split("-M")[0];
      }
      if(ctx.predefined_Matrix() != null){
         visit(ctx.predefined_Matrix());
         ctx.type = ctx.predefined_Matrix().type.split("-M")[0];
      }
      if(!visit(ctx.coords())){
         return false;
      }
      return true;
   }

   @Override public Boolean visitGetMatrixCommands(criptoParser.GetMatrixCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         ctx.type = symbolTable().get(ctx.ID().getText()).getType();
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         ctx.type = ctx.getCommands().type;
      }
      if(ctx.predefined_Matrix() != null){
         visit(ctx.predefined_Matrix());
         ctx.type = ctx.predefined_Matrix().type;
      }
      if(!visit(ctx.coords(0)) || !visit(ctx.coords(1))){
         return false;
      }
      return true;
   }

   @Override public Boolean visitGetRowCommands(criptoParser.GetRowCommandsContext ctx) {
      if(ctx.NUM() != null){
         if(ctx.ID(0) != null){
            if(!symbolTable().containsKey(ctx.ID(0).getText())){
               ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(0).getText() + " " + "as not been declared");
               ErrorHandling.errorCount();
               return false;
            }
            if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
               ErrorHandling.errorCount();
               return false;
            }
            ctx.type = symbolTable().get(ctx.ID(0).getText()).getType();
         }
      }else if(ctx.ID().size() == 2){
         if(!symbolTable().containsKey(ctx.ID(0).getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(0).getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().containsKey(ctx.ID(1).getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(1).getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(1).getText()).getType().equals("num")){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " is not a num type");
            ErrorHandling.errorCount();
            return false;
         }
         ctx.type = symbolTable().get(ctx.ID(0).getText()).getType();
      }else{
         if(!symbolTable().containsKey(ctx.ID(0).getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(0).getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(0).getText()).getType().equals("num")){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a num type");
            ErrorHandling.errorCount();
            return false;
         }
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         ctx.type = ctx.getCommands().type;
      }
      if(ctx.predefined_Matrix() != null){
         visit(ctx.predefined_Matrix());
         ctx.type = ctx.predefined_Matrix().type;
      }
      return true;
   }

   @Override public Boolean visitGetColCommands(criptoParser.GetColCommandsContext ctx) {
      if(ctx.NUM() != null){
         if(ctx.ID(0) != null){
            if(!symbolTable().containsKey(ctx.ID(0).getText())){
               ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(0).getText() + " " + "as not been declared");
               ErrorHandling.errorCount();
               return false;
            }
            if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
               ErrorHandling.errorCount();
               return false;
            }
            ctx.type = symbolTable().get(ctx.ID(0).getText()).getType();
         }
      }else if(ctx.ID().size() == 2){
         if(!symbolTable().containsKey(ctx.ID(0).getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(0).getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().containsKey(ctx.ID(1).getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(1).getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(1).getText()).getType().equals("num")){
            
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " is not a num type");
            ErrorHandling.errorCount();
            return false;
         }
         ctx.type = symbolTable().get(ctx.ID(0).getText()).getType();
      }else{
         if(!symbolTable().containsKey(ctx.ID(0).getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID(0).getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(0).getText()).getType().equals("num")){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a num type");
            ErrorHandling.errorCount();
            return false;
         }
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         ctx.type = ctx.getCommands().type;
      }
      if(ctx.predefined_Matrix() != null){
         visit(ctx.predefined_Matrix());
         ctx.type = ctx.predefined_Matrix().type;
      }
      return true;
   }

   @Override public Boolean visitZerosCommands(criptoParser.ZerosCommandsContext ctx) {
      if(!visit(ctx.coords())){
         return false;
      }
      ctx.type = ctx.types().getText()+"-M";
      return true;
   }

   @Override public Boolean visitToLineCommands(criptoParser.ToLineCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         ctx.type = symbolTable().get(ctx.ID().getText()).getType();
      }
      if(ctx.predefined_Matrix() != null){
         visit(ctx.predefined_Matrix());
         ctx.type = ctx.predefined_Matrix().type;
      }
      return true;
   }

   @Override public Boolean visitToMatrixCommands(criptoParser.ToMatrixCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         if(!visit(ctx.coords())){
            return false;
         }
         ctx.type = symbolTable().get(ctx.ID().getText()).getType();
      }
      if(ctx.predefined_Matrix() != null){
         visit(ctx.predefined_Matrix());
         ctx.type = ctx.predefined_Matrix().type;
      }
      return true;
   }

   @Override public Boolean visitToNumCommands(criptoParser.ToNumCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(symbolTable().get(ctx.ID().getText()).getType().equals("num")){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a num type");
            ErrorHandling.errorCount();
            return false;
         }
         if(symbolTable().get(ctx.ID().getText()).isMatrix()){
            ctx.type = "num-M";
         }else{
            ctx.type = "num";
         }
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         if(ctx.getCommands().type.equals("num")){
            ErrorHandling.printError(ctx,"Variable: is already a num");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.getCommands().type.contains("-M")){
            ctx.type = "num-M";
         }else{
            ctx.type = "num";
         }
      }
      if(ctx.operators() != null){
         if(!visit(ctx.operators())){
            return false;
         }
         if(ctx.operators().type.equals("num")){
            ErrorHandling.printError(ctx,"Variable: is already a num");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.operators().type.contains("-M")){
            ctx.type = "num-M";
         }else{
            ctx.type = "num";
         }
      }
      return true;
   }

   @Override public Boolean visitToBitCommands(criptoParser.ToBitCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(symbolTable().get(ctx.ID().getText()).getType().equals("bit")){
            ErrorHandling.printError(ctx,"Variable: is already a bit");
            ErrorHandling.errorCount();
            return false;
         }
         if(symbolTable().get(ctx.ID().getText()).isMatrix()){
            ctx.type = "bit-M";
         }else{
            ctx.type = "bit";
         }
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         if(ctx.getCommands().type.equals("bit")){
            ErrorHandling.printError(ctx,"Variable: is already a bit");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.getCommands().type.contains("-M")){
            ctx.type = "bit-M";
         }else{
            ctx.type = "bit";
         }
      }
      if(ctx.operators() != null){
         if(!visit(ctx.operators())){
            return false;
         }
         if(ctx.operators().type.equals("bit")){
            ErrorHandling.printError(ctx,"Variable: is already a bit");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.operators().type.contains("-M")){
            ctx.type = "bit-M";
         }else{
            ctx.type = "bit";
         }
      }
      return true;
   }

   @Override public Boolean visitToByteCommands(criptoParser.ToByteCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " +  ctx.ID().getText() + " " + "as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(symbolTable().get(ctx.ID().getText()).getType().equals("byte")){
            ErrorHandling.printError(ctx,"Variable: is already a byte");
            ErrorHandling.errorCount();
            return false;
         }
         if(symbolTable().get(ctx.ID().getText()).isMatrix()){
            ctx.type = "byte-M";
         }else{
            ctx.type = "byte";
         }
      }
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         if(ctx.getCommands().type.equals("byte")){
            ErrorHandling.printError(ctx,"Variable: is already a byte");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.getCommands().type.contains("-M")){
            ctx.type = "byte-M";
         }else{
            ctx.type = "byte";
         }
      }
      if(ctx.operators() != null){
         if(!visit(ctx.operators())){
            return false;
         }
         if(ctx.operators().type.equals("byte")){
            ErrorHandling.printError(ctx,"Variable: is already a byte");
            ErrorHandling.errorCount();
            return false;
         }
         if(ctx.operators().type.contains("-M")){
            ctx.type = "byte-M";
         }else{
            ctx.type = "byte";
         }
      }
      return true;
   }

   @Override public Boolean visitGetUserFuncCommands(criptoParser.GetUserFuncCommandsContext ctx) {
      if(!(symbolTable().containsKey(ctx.userFunc().ID().getText()) && symbolTable().get(ctx.userFunc().ID().getText()).getType().equals("func")))
      {
         ErrorHandling.printError(ctx,"Undifined function " + ctx.userFunc().ID().getText());
         ErrorHandling.errorCount();
         return false;
      }
      Functions f = new Functions(ctx.userFunc().ID().getText(),functionCreated.get(ctx.userFunc().ID().getText()).getType());
      List<criptoParser.UserArgContext> list = ctx.userFunc().userArg();
      for(int i = 0; i< list.size();i++)
      {
         if(list.get(i).NUM() != null)
         {
            f.addType("num");
         }
         else if(list.get(i).BYTE() != null)
         {
            f.addType("byte");
         }
         else if(list.get(i).ID() != null)
         {
            if(!symbolTable().containsKey(list.get(i).ID().getText()))
            {
               ErrorHandling.printError(ctx,"Variable: " +  list.get(i).ID().getText() + " " + "as not been declared");
               ErrorHandling.errorCount();
               return false;
            }
            
            f.addType(symbolTable().get(list.get(i).ID().getText()).getType());
         }
         else if(list.get(i).BIT() != null)
         {
            f.addType("bit");
         }
         else if(list.get(i).getCommands() != null)
         {
            if(!visit(list.get(i).getCommands())){
               return false;
            }
            f.addType(list.get(i).getCommands().type);
         }
      }
      
      
      if(functionCreated.get(ctx.userFunc().ID().getText()).equal(f))
      {
         ctx.type = functionCreated.get(ctx.userFunc().ID().getText()).getType();
         return true;
      }
      
      else
      {
         ErrorHandling.printError(ctx,"Bad Arguments");
         ErrorHandling.errorCount();
         return false;
      }

      
   }

   @Override public Boolean visitUserFunc(criptoParser.UserFuncContext ctx) {
      return true;
   }

   @Override public Boolean visitSubCommands(criptoParser.SubCommandsContext ctx) {
         if(!symbolTable().containsKey(ctx.ID(0).getText())){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " does not exist");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
      if(ctx.ID(1) != null){
         if(!symbolTable().containsKey(ctx.ID(1).getText())){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(1).getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
      }
      return true;
   }

   @Override public Boolean visitShiftRowsCommands(criptoParser.ShiftRowsCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.ID().getText())){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
         ErrorHandling.errorCount();
         return false;
      }
      if(!visit(ctx.coords())){
         return false;
      }
      return true;
   }

   @Override public Boolean visitShiftColsCommands(criptoParser.ShiftColsCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.ID().getText())){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
         ErrorHandling.errorCount();
         return false;
      }
      if(!visit(ctx.coords())){
         return false;
      }
      return true;
   }

   @Override public Boolean visitSetValueCommands(criptoParser.SetValueCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.ID(0).getText())){ 
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
         ErrorHandling.errorCount();
         return false;
      }
      if(ctx.ID(1) != null){
         
         if(!symbolTable().containsKey(ctx.ID(1).getText())){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
            ErrorHandling.errorCount();
         return false;
         }
         if(symbolTable().get(ctx.ID(1).getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
      }
      if(!visit(ctx.coords())){
         return false;
      }  
      return true;
   }

   @Override public Boolean visitSetRowCommands(criptoParser.SetRowCommandsContext ctx) {
      String typeId0;
      if(!(symbolTable().containsKey(ctx.ID(0).getText()))){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      typeId0 = symbolTable().get(ctx.ID(0).getText()).type;
      if(ctx.NUM() != null){
         if(ctx.ID().size() == 2){
            if(!(symbolTable().containsKey(ctx.ID(1).getText()))){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!typeId0.equals(symbolTable().get(ctx.ID(1).getText()).type)){
               ErrorHandling.printError(ctx,"Diferent matrix types in variable arguments");
               ErrorHandling.errorCount();
               return false;
            }
         }
      }else{
         if(ctx.ID().size() == 3){
            if(!(symbolTable().containsKey(ctx.ID(1).getText()))){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!(symbolTable().containsKey(ctx.ID(2).getText()))){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(2).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!symbolTable().get(ctx.ID(2).getText()).type.equals("num")){
               ErrorHandling.printError(ctx,"Row number argument " + ctx.ID(2).getText() + " is not num type");
               ErrorHandling.errorCount();
               return false;
            }
         }
         return true;
      } 
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         if(!typeId0.equals(ctx.getCommands().type)){
               ErrorHandling.printError(ctx,"Diferent matrix types in variable arguments");
               ErrorHandling.errorCount();
               return false;
         }
      }
      return true;
   }

   @Override public Boolean visitSetMatrixCommands(criptoParser.SetMatrixCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.ID(0).getText())){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      if(!symbolTable().get(ctx.ID(0).getText()).isMatrix()){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " is not a matrix");
         ErrorHandling.errorCount();
         return false;
      }
      if(ctx.ID(1) != null){
         if(!symbolTable().containsKey(ctx.ID(1).getText())){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID(1).getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
      }
      if(!visit(ctx.coords())){
         return false;
      }
      return true;
   }

   @Override public Boolean visitSetColCommands(criptoParser.SetColCommandsContext ctx) {
      String typeId0;
      if(!(symbolTable().containsKey(ctx.ID(0).getText()))){
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      typeId0 = symbolTable().get(ctx.ID(0).getText()).type;
      if(ctx.NUM() != null){
         if(ctx.ID().size() == 2){
            if(!(symbolTable().containsKey(ctx.ID(1).getText()))){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!typeId0.equals(symbolTable().get(ctx.ID(1).getText()).type)){
               ErrorHandling.printError(ctx,"Diferent matrix types in variable arguments");
               ErrorHandling.errorCount();
               return false;
            }
         }
      }else{
         if(ctx.ID().size() == 3){
            if(!(symbolTable().containsKey(ctx.ID(1).getText()))){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!(symbolTable().containsKey(ctx.ID(2).getText()))){
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(2).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            if(!symbolTable().get(ctx.ID(2).getText()).type.equals("num")){
               ErrorHandling.printError(ctx,"Column number argument " + ctx.ID(2).getText() + " is not num type");
               ErrorHandling.errorCount();
               return false;
            }
         }
      } 
      if(ctx.getCommands() != null){
         if(!visit(ctx.getCommands())){
            return false;
         }
         if(!typeId0.equals(ctx.getCommands().type)){
               ErrorHandling.printError(ctx,"Diferent matrix types in variable arguments");
               ErrorHandling.errorCount();
               return false;
         }
      }
      return true;
   }

   @Override public Boolean visitRotateColCommands(criptoParser.RotateColCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.ID().getText()))
      {
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      else
      {
         if(!symbolTable().get(ctx.ID().getText()).isMatrix())
         {
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " must be a matrix");
            ErrorHandling.errorCount();
            return false;
         }
         return true;
      }
   }

   @Override public Boolean visitRotateLineCommands(criptoParser.RotateLineCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.ID().getText()))
      {
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      else
      {
         if(!symbolTable().get(ctx.ID().getText()).isMatrix())
         {
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " must be a matrix");
            ErrorHandling.errorCount();
            return false;
            
         }
         return true;

      }
   }

   @Override public Boolean visitUserFuncCommands(criptoParser.UserFuncCommandsContext ctx) {
      if(!symbolTable().containsKey(ctx.userFunc().ID().getText()))
      {
         ErrorHandling.printError(ctx,ctx.userFunc().ID().getText() + " function does not exist");
         ErrorHandling.errorCount();
         return false;
      }
      Functions f = new Functions(ctx.userFunc().ID().getText(),"nothing");
      List<criptoParser.UserArgContext> list = ctx.userFunc().userArg();
      for(int i = 0; i< list.size();i++)
      {
         if(list.get(i).NUM() != null)
         {
            f.addType("num");
         }
         else if(list.get(i).BYTE() != null)
         {
            f.addType("byte");
         }
         else if(list.get(i).ID() != null)
         {
            if(!symbolTable().containsKey(list.get(i).ID().getText()))
            {
               ErrorHandling.printError(ctx,"Variable: " + list.get(i).ID().getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            f.addType(symbolTable().get(list.get(i).ID().getText()).getType());
         }
         else if(list.get(i).BIT() != null)
         {
            f.addType("bit");
         }
         else if(list.get(i).getCommands() != null)
         {
            f.addType(list.get(i).getCommands().type);
         }
      }
      
      if(!functionCreated.get(ctx.userFunc().ID().getText()).equal(f))
      {
         ErrorHandling.printError(ctx,"Function: " + ctx.userFunc().ID().getText() + " does not exist or has bad arguments");
         ErrorHandling.errorCount();
         return false;
      }
      return true;
   }

   @Override public Boolean visitPredefined_MatrixS_Box(criptoParser.Predefined_MatrixS_BoxContext ctx) {
      ctx.type = "byte-M";
      return true;
   }

   @Override public Boolean visitPredefined_MatrixS_BoxInverted(criptoParser.Predefined_MatrixS_BoxInvertedContext ctx) {
      ctx.type = "byte-M";
      return true;
   }

   @Override public Boolean visitPredefined_MatrixS_rcon(criptoParser.Predefined_MatrixS_rconContext ctx) {
      ctx.type = "byte-M";
      return true;
   }

   @Override public Boolean visitPredefined_MatrixL(criptoParser.Predefined_MatrixLContext ctx) {
      ctx.type = "byte-M";
      return true;
   }

   @Override public Boolean visitPredefined_MatrixE(criptoParser.Predefined_MatrixEContext ctx) {
      ctx.type = "byte-M";
      return true;
   }

   @Override public Boolean visitCoords(criptoParser.CoordsContext ctx) {
      Boolean res = visit(ctx.coord(0));
      Boolean res2 = visit(ctx.coord(1));
      return res && res2;
   }

   @Override public Boolean visitCoord(criptoParser.CoordContext ctx) {
      
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " does not exist");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID().getText()).getType().equals("num")){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a Num");
            ErrorHandling.errorCount();
            return false;
         }
      }
      if(ctx.getCommands() != null){
          if(!visit(ctx.getCommands())){
             return false;
          }
          if(!ctx.getCommands().type.equals("num")){
            ErrorHandling.printError(ctx,"Funtion does not return a Num");
            ErrorHandling.errorCount();
            return false;
         }
      }
      return true;
   }

   @Override public Boolean visitCondBYTE(criptoParser.CondBYTEContext ctx) {
      ctx.type = "byte";
      return true;
   }

   @Override public Boolean visitCondOperation(criptoParser.CondOperationContext ctx) {
      visit(ctx.cond(0));
      visit(ctx.cond(1));
      List<String> boolOper = Arrays.asList("=","!=","&&","||","~");
      List<String> numBitByteOper = Arrays.asList("=",">=",">","<","<=","!=");
      if(ctx.cond(0).type.equals("boolean") && ctx.cond(1).type.equals("boolean")){
         if(!boolOper.contains(ctx.OPERATOR().getText())){
            ErrorHandling.printError(ctx,"Conditions are not compatible");
            ErrorHandling.errorCount();
            return false;
         }
      }
      else if(!ctx.cond(0).type.equals(ctx.cond(1).type)){
         ErrorHandling.printError(ctx,"Conditions are not compatible");
         ErrorHandling.errorCount();
         return false;
      }else{
         if(!numBitByteOper.contains(ctx.OPERATOR().getText())){
            return false;
         }
      }
      ctx.type = "boolean";
      return true;
   }

   @Override public Boolean visitCondParent(criptoParser.CondParentContext ctx) {
      if(!visit(ctx.cond())){
         return false;
      }
      ctx.type = ctx.cond().type;
      return true;
   }

   @Override public Boolean visitCondNUM(criptoParser.CondNUMContext ctx) {
      ctx.type = "num";
      return true;
   }

   @Override public Boolean visitCondBIT(criptoParser.CondBITContext ctx) {
      ctx.type = "bit";
      return true;
   }

   @Override public Boolean visitCondOperators(criptoParser.CondOperatorsContext ctx) {
      if(!visit(ctx.operators())){
         return false;
      }
      ctx.type = ctx.operators().type;
      return true;
   }

   @Override public Boolean visitCondVarExpr(criptoParser.CondVarExprContext ctx) {
      String varName = ctx.ID().getText();
      if(!symbolTable().containsKey(varName))
      {
         ErrorHandling.printError(ctx,"Variable: " + varName + " has not been declared");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = symbolTable().get(ctx.ID().getText()).getType();
      return true;
   }

   @Override public Boolean visitOperByte(criptoParser.OperByteContext ctx) {
      ctx.type = "byte";
      return true;
   }

   @Override public Boolean visitOperBit(criptoParser.OperBitContext ctx) {
      ctx.type = "bit";
      return true;
   }

   @Override public Boolean visitOperID(criptoParser.OperIDContext ctx) {
      boolean res = true;
      String varName = ctx.ID().getText();
      if(!symbolTable().containsKey(varName))
      {
         ErrorHandling.printError(ctx,"Variable: " + varName + " has not been declared");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = symbolTable().get(ctx.ID().getText()).getType();
      return res;
   }

   @Override public Boolean visitOperParentheses(criptoParser.OperParenthesesContext ctx) {
      if(!visit(ctx.operators())){
         return false;
      }
      ctx.type = ctx.operators().type;
      return true;
   }

   @Override public Boolean visitOperGetCommands(criptoParser.OperGetCommandsContext ctx) {
      if(!visit(ctx.getCommands())){
         return false;
      }
      ctx.type = ctx.getCommands().type;
      return true;
   }

   @Override public Boolean visitOperPow(criptoParser.OperPowContext ctx) {
      if(!visit(ctx.operators(0)) || !visit(ctx.operators(1))){
         return false;
      } 
      if(!ctx.operators(0).type.equals(ctx.operators(1).type)){
         ErrorHandling.printError(ctx,"Variables: are not the same type");
         ErrorHandling.errorCount();
         return false;
      }
      if(!ctx.operators(0).type.equals("num")){
         ErrorHandling.printError(ctx,"Operators must be a Num type");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = ctx.operators(0).type;
      return true;
   }

   @Override public Boolean visitOperNum(criptoParser.OperNumContext ctx) {
      ctx.type = "num";
      return true;
   }

   @Override public Boolean visitOperDivMultRest(criptoParser.OperDivMultRestContext ctx) {
      if(!visit(ctx.operators(0)) || !visit(ctx.operators(1))){
         return false;
      } 
      if(!ctx.operators(0).type.equals(ctx.operators(1).type)){
         ErrorHandling.printError(ctx,"Operators are not the same type");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = ctx.operators(0).type;
      return true;
   }

   @Override public Boolean visitOperAddSub(criptoParser.OperAddSubContext ctx) {
      if(!visit(ctx.operators(0)) || !visit(ctx.operators(1))){
         return false;
      } 
      if(!ctx.operators(0).type.equals(ctx.operators(1).type)){
         ErrorHandling.printError(ctx,"Operators are not the same type");
         ErrorHandling.errorCount();
         return false;
      }
      ctx.type = ctx.operators(0).type;
      return true;
   }

   @Override public Boolean visitWhileLoop(criptoParser.WhileLoopContext ctx) {
      if(ctx.blockCode() != null)
      {
         if(ctx.blockCode().getText().contains("out"))
         {
            ErrorHandling.printError(ctx,"While cannot return a value");
            ErrorHandling.errorCount();  
            return false;

         }
      }
      Boolean res = visit(ctx.blockCode());
      if(res != null)
      {  
         return visit(ctx.cond()) && res;
      }
      
      return visit(ctx.cond());
   }

   @Override public Boolean visitIfCond(criptoParser.IfCondContext ctx) {
      if(ctx.blockCode() != null)
      {
         if(ctx.blockCode().getText().contains("out"))
         {
            ErrorHandling.printError(ctx,"IF cannot return a value");
            ErrorHandling.errorCount();  
            return false;

         }
      }
      Boolean res = visit(ctx.blockCode());
      if(res != null)
      {  
         return visit(ctx.cond()) && res;
      }
      
      return visit(ctx.cond());
      
   }

   @Override public Boolean visitForLoop(criptoParser.ForLoopContext ctx) {
      boolean resLeft = visit(ctx.forLeft());
      boolean resRight = visit(ctx.forRight());
      Boolean visitBlock = null;
      if(ctx.blockCode() != null){
         visitBlock = visit(ctx.blockCode());
      }
      
      
      if(visitBlock != null){
         return resLeft && resRight && visitBlock;
      }
      return resLeft && resRight;
   }

   @Override public Boolean visitForLDeclareOption(criptoParser.ForLDeclareOptionContext ctx) {
      boolean res = visit(ctx.declareFor());
      return res;
   }

   @Override public Boolean visitForAssign(criptoParser.ForAssignContext ctx) {
      Boolean temp = visit(ctx.assignmentFor());
      return temp;
   }

   @Override public Boolean visitAssignmentFor(criptoParser.AssignmentForContext ctx) {
      if(!symbolTable().containsKey(ctx.ID(0).getText()))
      {
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " does not exist");
         ErrorHandling.errorCount();
      }
      else
      {
         if(ctx.ID(1) != null)
         {
            if(!symbolTable().containsKey(ctx.ID(1).getText()))
            {
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
         }
      }
      return true;
   }

   @Override public Boolean visitForLVariable(criptoParser.ForLVariableContext ctx) {
      
      String varName = ctx.ID().getText();
      if(!symbolTable().containsKey(varName))
      {
         ErrorHandling.printError(ctx,"Variable: " + varName + " has not been declared");
         ErrorHandling.errorCount();
         return false;
      }
      if(!(symbolTable().get(varName).getType()).equals("num"))
      {
         ErrorHandling.printError(ctx,"Variable: " + varName + " must be a Num type");
         ErrorHandling.errorCount();
         return false;
      }
      return true;
   }

   @Override public Boolean visitForLNumber(criptoParser.ForLNumberContext ctx) {
      
      ctx.min = ctx.NUM().getText();
      return true;
   }

   @Override public Boolean visitForRVariable(criptoParser.ForRVariableContext ctx) {
      
      String varName = ctx.ID().getText();
      if(!symbolTable().containsKey(varName))
      {
         ErrorHandling.printError(ctx,"Variable: " + varName + " has not been declared");
         ErrorHandling.errorCount();
         return false;
      }
      if(!(symbolTable().get(varName).getType()).equals("num"))
      {
         ErrorHandling.printError(ctx,"Variable: " + varName + " must be a Num type");
         ErrorHandling.errorCount();
         return false;
      }
     
      return true;
      
   }

   @Override public Boolean visitForRNumber(criptoParser.ForRNumberContext ctx) {
      
      ctx.n = ctx.NUM().getText();
      return true;
   }

   @Override public Boolean visitSwitchAxisCommands(criptoParser.SwitchAxisCommandsContext ctx) {
      if(ctx.ID() != null){
         if(!symbolTable().containsKey(ctx.ID().getText())){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " as not been declared");
            ErrorHandling.errorCount();
            return false;
         }
         if(!symbolTable().get(ctx.ID().getText()).isMatrix()){
            ErrorHandling.printError(ctx,"Variable: " + ctx.ID().getText() + " is not a matrix");
            ErrorHandling.errorCount();
            return false;
         }
      }
      return true;
   }

   @Override public Boolean visitDeclareFor(criptoParser.DeclareForContext ctx) {
      if(symbolTable().containsKey(ctx.ID(0).getText()))
      {
         ErrorHandling.printError(ctx,"Variable: " + ctx.ID(0).getText() + " already exists");
         ErrorHandling.errorCount();
      }
      
      else
      {
         if(ctx.ID(1) != null)
         {
            if(!symbolTable().containsKey(ctx.ID(1).getText()))
            {
               ErrorHandling.printError(ctx,"Variable: " + ctx.ID(1).getText() + " does not exist");
               ErrorHandling.errorCount();
               return false;
            }
            symbolTable().put(ctx.ID(0).getText(),symbolTable().get(ctx.ID(1)));
         }
         else
         {
            symbolTable().put(ctx.ID(0).getText(), new Symbol(ctx.ID(0).getText(),"num"));
         }
      }
      return true;
   }
}
