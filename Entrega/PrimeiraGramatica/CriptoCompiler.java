import org.stringtemplate.v4.*;
import java.util.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;


public class CriptoCompiler extends criptoBaseVisitor<ST> {
   private STGroup templates = new STGroupFile("pythonPattern.stg");
   private String className = "";

   public String getClassName() {
      return this.className;
   }

   @Override public ST visitProgram(criptoParser.ProgramContext ctx){
      return visit(ctx.init());
   }

   @Override public ST visitInit(criptoParser.InitContext ctx) {
      ST res = templates.getInstanceOf("program");
      this.className = ctx.fileName().ID().getText();

      res.add("className", this.className);
      res.add("dataSizeBlock", ctx.fileName().NUM(0).getText());
      res.add("keySizeBlock", ctx.fileName().NUM(1).getText());
      
      if(ctx.line() != null){
         ctx.line().stream().forEach(x -> res.add("program", visit(x).render()));
      }
      if(ctx.function() != null){
         ctx.function().stream().forEach(x -> res.add("functions", visit(x).render()));
      }
      if(ctx.import1() != null){
         ctx.import1().stream().forEach(x -> res.add("header",visit(x).render()));
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

   @Override public ST visitFileName(criptoParser.FileNameContext ctx) {
      return visitChildren(ctx);
   }

   @Override public ST visitImport1(criptoParser.Import1Context ctx) {
      ST res = templates.getInstanceOf("headerImport");
      res.add("name", ctx.ID().getText());
      return res;
   }

   @Override public ST visitLine(criptoParser.LineContext ctx) {
      return visit(ctx.action());
   }

   @Override public ST visitSwitchAxisCommands(criptoParser.SwitchAxisCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("class", ctx.ID().getText());
      res.add("funcName", "switch_axis");
      return res;
   }

   @Override public ST visitActionDeclare(criptoParser.ActionDeclareContext ctx) {
      return visit(ctx.declare());
   }

   @Override public ST visitActionForLoop(criptoParser.ActionForLoopContext ctx) {
      return visit(ctx.forLoop());
   }

   @Override public ST visitActionIfCond(criptoParser.ActionIfCondContext ctx) {
      return visit(ctx.ifCond());
   }

   @Override public ST visitActionWhileCond(criptoParser.ActionWhileCondContext ctx) {
      return visit(ctx.whileLoop());
   }

   @Override public ST visitActionCommands(criptoParser.ActionCommandsContext ctx) {
      return visit(ctx.commands());
   }

   @Override public ST visitActionAssign(criptoParser.ActionAssignContext ctx) {
      return visit(ctx.assignment());
   }

   @Override public ST visitInterval(criptoParser.IntervalContext ctx) {
      return visitChildren(ctx);
   }

   @Override public ST visitBlockCode(criptoParser.BlockCodeContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", "return ");
      if(ctx.getCommands() != null) {
         res.add("arg", visit(ctx.getCommands()).render());
      }
      else if(ctx.BYTE() != null) {
         res.add("arg", ctx.BYTE().getText());
      }
      else if(ctx.BIT() != null) {
         res.add("arg", ctx.BIT().getText().replace("0b", ""));
      }
      else if(ctx.NUM() != null) {
         res.add("arg", ctx.NUM().getText());
      }
      else if(ctx.ID() != null) {
         res.add("arg", ctx.ID().getText());
      }
      else {
         return null;
      }
      
      return res;
   }

   @Override public ST visitDeclare(criptoParser.DeclareContext ctx) {
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

   @Override public ST visitAssignment(criptoParser.AssignmentContext ctx) {
      ST res = templates.getInstanceOf("assign");
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

   @Override public ST visitValueGetCommands(criptoParser.ValueGetCommandsContext ctx) {
      return visit(ctx.getCommands());
   }

   @Override public ST visitValuePredefined_Matrix(criptoParser.ValuePredefined_MatrixContext ctx) {
      return visit(ctx.predefined_Matrix());
   }

   @Override public ST visitValueNUM(criptoParser.ValueNUMContext ctx) {      
      ST res = templates.getInstanceOf("string");
      res.add("arg", visitNUM(ctx.NUM().getText()).render());
      return res;
   }

   @Override public ST visitValueBIT(criptoParser.ValueBITContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", visitNUM(ctx.BIT().getText()).render());
      return res;
   }

   @Override public ST visitValueID(criptoParser.ValueIDContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.ID().getText());
      return res;
   }

   @Override public ST visitValueBYTE(criptoParser.ValueBYTEContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", visitNUM(ctx.BYTE().getText()).render());
      return res;
   }

   @Override public ST visitValueMatrix(criptoParser.ValueMatrixContext ctx) {
      return visit(ctx.matrix());
   }

   @Override public ST visitValueOperators(criptoParser.ValueOperatorsContext ctx) {
      return visit(ctx.operators());
   }

   @Override public ST visitVariableNames(criptoParser.VariableNamesContext ctx) {
      return visitChildren(ctx);
   }

   @Override public ST visitMatrix(criptoParser.MatrixContext ctx) {
      ST res = templates.getInstanceOf("string");
      Iterator<criptoParser.LineMatrixContext> it = ctx.lineMatrix().iterator();
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

   @Override public ST visitLineMatrix(criptoParser.LineMatrixContext ctx) {
      ST res = templates.getInstanceOf("string");
      Iterator<TerminalNode> itNUM = ctx.NUM().iterator();
      Iterator<TerminalNode> itBIT = ctx.BIT().iterator();
      Iterator<TerminalNode> itBYTE = ctx.BYTE().iterator();
      Iterator<TerminalNode> it = itNUM;
      ctx.type = "num";
      if(itBIT.hasNext()){
         it = itBIT;
         ctx.type = "bit";
      }
      else if(itBYTE.hasNext()) {
         it = itBYTE;
         ctx.type = "byte"; 
      }
      
      while(it.hasNext()){
         res.add("arg", it.next().getText().replace("0b", ""));
         if(it.hasNext()){
            res.add("arg", ", ");
         }
      }
      return res;
   }

   @Override public ST visitFunction(criptoParser.FunctionContext ctx) {
      ST res = templates.getInstanceOf("funct");
      res.add("name", ctx.ID(0).getText());
      Iterator<TerminalNode> it = ctx.ID().iterator();
      while(it.hasNext()){
         String t = it.next().getText();
         if(t.compareTo(ctx.ID(0).getText()) != 0){
            res.add("params", t);
            if(it.hasNext()){
               res.add("params", ", ");
            }
         }
      }
      Iterator<criptoParser.LineContext> it2 = ctx.blockCode().line().iterator();
      while(it2.hasNext()){
         res.add("body", visit(it2.next()));
      }
      if(visit(ctx.blockCode()) != null) {
         res.add("return", visit(ctx.blockCode()).render());
      }
      
      return res;
   }

   @Override public ST visitFunctionTypes(criptoParser.FunctionTypesContext ctx) {
      return visitChildren(ctx);
   }

   @Override public ST visitTypes(criptoParser.TypesContext ctx) {
      return visitChildren(ctx);
   }

   @Override public ST visitGetValueCommands(criptoParser.GetValueCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_value");
      
      if(ctx.ID() != null) {
         res.add("class", ctx.ID().getText());
      }else if(ctx.predefined_Matrix() != null){
         res.add("class", visit(ctx.predefined_Matrix()).render());
      }else if(visit(ctx.getCommands()) != null){
         res.add("class", visit(ctx.getCommands()).render());
      }

      res.add("args", visit(ctx.coords()).render());
      return res;
   }

   @Override public ST visitGetMatrixCommands(criptoParser.GetMatrixCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_matrix");

      if(ctx.ID() != null) {
         res.add("class", ctx.ID().getText());
      }else if(visit(ctx.predefined_Matrix()) != null){
         res.add("class", visit(ctx.predefined_Matrix()).render());
      }else if(visit(ctx.getCommands()) != null){
         res.add("class", visit(ctx.getCommands()).render());
      }
      res.add("args", visit(ctx.coords(0)).render());
      res.add("args", ", " + visit(ctx.coords(1)).render());
      return res;
   }

   @Override public ST visitGetRowCommands(criptoParser.GetRowCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_row");
      int cont = 0;
      
      if(ctx.predefined_Matrix() != null) {
         res.add("class", visit(ctx.predefined_Matrix()).render());
      }else if(ctx.getCommands() != null){
         res.add("class", visit(ctx.getCommands()).render());
      }else if(ctx.ID(cont) != null){
         res.add("class", ctx.ID(cont++).getText());
      }

      if(ctx.NUM() != null){
         res.add("args", ctx.NUM().getText());
      }else if(ctx.ID(cont) != null){
         res.add("args", ctx.ID(cont).getText());
      }
      return res;
   }

   @Override public ST visitGetColCommands(criptoParser.GetColCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_col");
      int counter = 0;

      if(ctx.predefined_Matrix() != null) {
         res.add("class", visit(ctx.predefined_Matrix()).render());
      }else if(ctx.getCommands() != null){
         res.add("class", visit(ctx.getCommands()).render());
      }
      else if(ctx.ID(counter) != null){
         res.add("class", ctx.ID(counter++).getText());
      }

      if(ctx.NUM() != null){
         res.add("args", ctx.NUM().getText());
      }else if(ctx.ID(counter) != null){
         res.add("args", ctx.ID(counter).getText());
      }
      return res;
   }

   @Override public ST visitZerosCommands(criptoParser.ZerosCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "zeros");
      res.add("class", "Matrix");
      res.add("args", "\"" + ctx.types().getText() + "\", ");
      res.add("args",visit(ctx.coords()).render());
      return res;
   }

   @Override public ST visitToLineCommands(criptoParser.ToLineCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "to_line");

      if(ctx.ID() != null){
         res.add("class", ctx.ID().getText());
      }else if(visit(ctx.predefined_Matrix()) != null){
         res.add("class", visit(ctx.predefined_Matrix()).render());
      }
      return res;
   }

   @Override public ST visitToMatrixCommands(criptoParser.ToMatrixCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "reshape");

      if(ctx.ID() != null){
         res.add("class", ctx.ID().getText());
      }else if(visit(ctx.predefined_Matrix()) != null){
         res.add("class", visit(ctx.predefined_Matrix()).render());
      }

      res.add("args", visit(ctx.coords()).render());
      return res;
   }

   @Override public ST visitToNumCommands(criptoParser.ToNumCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "to_num");

      if(ctx.ID() != null){
         res.add("class", ctx.ID().getText());
      }else if(ctx.BYTE() != null){
         res.add("class", ctx.BYTE().getText());
      }else if(ctx.BIT() != null){
         res.add("class", ctx.BIT().getText().replace("0b", ""));
      }else if(ctx.getCommands() != null){
         res.add("class", visit(ctx.getCommands()).render());
      }else if(visit(ctx.operators()) != null){
         res.add("class", visit(ctx.operators()).render());
      }
      return res;
   }

   @Override public ST visitToByteCommands(criptoParser.ToByteCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "to_byte");

      if(ctx.ID() != null){
         res.add("class", ctx.ID().getText());
      }else if(ctx.NUM() != null){
         res.add("class", ctx.NUM().getText());
      }else if(ctx.BIT() != null){
         res.add("class", ctx.BIT().getText().replace("0b", ""));
      }else if(ctx.getCommands() != null){
         res.add("class", visit(ctx.getCommands()).render());
      }else if(visit(ctx.operators()) != null){
         res.add("class", visit(ctx.operators()).render());
      }
      return res;
   }

   @Override public ST visitToBitCommands(criptoParser.ToBitCommandsContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "to_bit");

      if(ctx.ID() != null){
         res.add("class", ctx.ID().getText());
      }else if(ctx.NUM() != null){
         res.add("class", ctx.NUM().getText());
      }else if(ctx.BYTE() != null){
         res.add("class", ctx.BYTE().getText().replace("0b", ""));
      }else if(visit(ctx.getCommands()) != null){
         res.add("class", visit(ctx.getCommands()).render());
      }else if(visit(ctx.operators()) != null){
         res.add("class", visit(ctx.operators()).render());
      }
      return res;
   }

   @Override public ST visitGetUserFuncCommands(criptoParser.GetUserFuncCommandsContext ctx) {
      return visit(ctx.userFunc());
   }

   @Override public ST visitUserFunc(criptoParser.UserFuncContext ctx) {
      ST res = templates.getInstanceOf("userFunction");
      res.add("funcName", ctx.ID().getText());
      res.add("class", this.className);
      boolean addComma = false;
      
      Iterator<criptoParser.UserArgContext> it = ctx.userArg().iterator();
      while(it.hasNext()){
         if(addComma) {
            res.add("args", ", ");
         }
         res.add("args", visit(it.next()).render());
         addComma = true;
      }

      return res;
   }

   @Override public ST visitUserArg(criptoParser.UserArgContext ctx) {
      ST res = templates.getInstanceOf("string");
      
      if(ctx.NUM() != null){
         res.add("arg", visitNUM(ctx.NUM().getText()).render());
      }
      else if(ctx.BYTE() != null){
         res.add("arg", visitNUM(ctx.BYTE().getText()).render());
      }
      else if(ctx.BIT() != null){
         res.add("arg", visitNUM(ctx.BIT().getText()).render());
      }
      else if(ctx.getCommands() != null){
         res.add("arg", visit(ctx.getCommands()).render());
      }
      else if(ctx.ID() != null){
         res.add("arg", ctx.ID().getText());
      }
      return res;
   }

   @Override public ST visitSubCommands(criptoParser.SubCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName", "sub_bytes");
      res.add("class", ctx.ID(0).getText());
      
      if(ctx.ID(1) != null) {
         res.add("args", ctx.ID(1).getText());
      }else if(visit(ctx.predefined_Matrix()) != null){
         res.add("args",visit(ctx.predefined_Matrix()).render());
      }
      return res;
   }

   @Override public ST visitShiftRowsCommands(criptoParser.ShiftRowsCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "rotate_row");
      res.add("class", ctx.ID().getText());
      res.add("args", visit(ctx.coords()).render());
      return res;
   }

   @Override public ST visitShiftColsCommands(criptoParser.ShiftColsCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "rotate_col");
      res.add("class", ctx.ID().getText());
      res.add("args", visit(ctx.coords()).render());
      return res;
   }

   @Override public ST visitSetValueCommands(criptoParser.SetValueCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "set_value");
      res.add("class", ctx.ID(0).getText());
      res.add("args", visit(ctx.coords()).render() + ", ");
      if(ctx.BIT() != null) {
         res.add("args", ctx.BIT().getText().replace("0b", ""));
      }
      else if(ctx.BYTE() != null) {
         res.add("args", ctx.BYTE().getText());
      }
      else if(ctx.NUM() != null) {
         res.add("args", ctx.NUM().getText());
      }
      else if(ctx.ID(1) != null) {
         res.add("args", ctx.ID(1).getText());
      }
      else if(ctx.getCommands() != null) {
         res.add("args", visit(ctx.getCommands()).render());
      }
      return res;
   }

   @Override public ST visitSetMatrixCommands(criptoParser.SetMatrixCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "overwrite");
      res.add("class", ctx.ID(0).getText());
      res.add("args", ctx.ID(1).getText() + ", ");
      res.add("args", visit(ctx.coords()).render());
      return res;
   }

   @Override public ST visitSetRowCommands(criptoParser.SetRowCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "set_row");
      res.add("class", ctx.ID(0).getText());
      int count = 1;

      if(ctx.getCommands() != null){
         res.add("args", visit(ctx.getCommands()).render());
      }
      else if(ctx.ID(count) != null){
         res.add("args", ctx.ID(count++).getText());
      }
      res.add("args", ", ");

      if(ctx.NUM() != null){
         res.add("args", ctx.NUM().getText());
      }else if(ctx.ID(count) != null){
         res.add("args", ctx.ID(count).getText());
      }
      return res;
   }

   @Override public ST visitSetColCommands(criptoParser.SetColCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      int count = 1;
      res.add("funcName",  "set_col");
      res.add("class", ctx.ID(0).getText());

      if(ctx.getCommands() != null){
         res.add("args", visit(ctx.getCommands()).render());
      }else if(ctx.ID(count) != null){
         res.add("args", ctx.ID(count).getText());
         count++;
         
      }
      res.add("args", ", ");

      if(ctx.NUM() != null){
         res.add("args", ctx.NUM().getText());
      }else if(ctx.ID(count) != null){
         res.add("args", ctx.ID(count).getText());
      }
      return res;
   }

   @Override public ST visitRotateColCommands(criptoParser.RotateColCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "rotate_col");
      res.add("class", ctx.ID().getText());
      res.add("args", ctx.NUM(0).getText() + ", ");
      res.add("args", ctx.NUM(0).getText());
      return res;
   }

   @Override public ST visitRotateLineCommands(criptoParser.RotateLineCommandsContext ctx) {
      ST res = templates.getInstanceOf("predefinedFunctions");
      res.add("funcName",  "rotate_line");
      res.add("class", ctx.ID().getText());
      res.add("args", ctx.NUM(0).getText() + ", ");
      res.add("args", ctx.NUM(0).getText());
      return res;
   }

   @Override public ST visitUserFuncCommands(criptoParser.UserFuncCommandsContext ctx) {
      return visit(ctx.userFunc());
   }

   @Override public ST visitPredefined_MatrixS_Box(criptoParser.Predefined_MatrixS_BoxContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("class", "Matrix");
      res.add("funcName", "get_rijndael_s_box");
      return res;
   }

   @Override public ST visitPredefined_MatrixS_BoxInverted(criptoParser.Predefined_MatrixS_BoxInvertedContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("class", "Matrix");
      res.add("funcName", "get_rijndael_s_box_inverted");
      return res;
   }

   @Override public ST visitPredefined_MatrixS_rcon(criptoParser.Predefined_MatrixS_rconContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_rcon");
      res.add("class", "Matrix");
      return res;
   }

   @Override public ST visitPredefined_MatrixL(criptoParser.Predefined_MatrixLContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_L");
      res.add("class", "Matrix");
      return res;
   }

   @Override public ST visitPredefined_MatrixE(criptoParser.Predefined_MatrixEContext ctx) {
      ST res = templates.getInstanceOf("getPredefinedFunctions");
      res.add("funcName", "get_E");
      res.add("class", "Matrix");
      return res;
   }

   @Override public ST visitCoords(criptoParser.CoordsContext ctx) {
      ST res = templates.getInstanceOf("coordenates");
      res.add("x",visit(ctx.coord(0)).render());
      res.add("y",visit(ctx.coord(1)).render());
      return res;
   }

   @Override public ST visitCoord(criptoParser.CoordContext ctx) {
      ST res = templates.getInstanceOf("string");
      if(ctx.NUM() != null){
         res.add("arg", ctx.NUM().getText());
      }else if(ctx.ID() != null){
         res.add("arg", ctx.ID().getText());
      }else if(ctx.getCommands() != null){
         res.add("arg", visit(ctx.getCommands()).render());
      }
      return res;
   }

   @Override public ST visitCondBYTE(criptoParser.CondBYTEContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.BYTE().getText());
      return res;
   }

   @Override public ST visitCondOperation(criptoParser.CondOperationContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", visit(ctx.cond(0)).render());
      String operator = visitOPERATOR(ctx.OPERATOR().getText()).render();
      if(operator.equals("=")) operator = "==";
      res.add("arg", " " + operator + " ");
      res.add("arg", visit(ctx.cond(1)).render());
      return res;
   }

   @Override public ST visitCondParent(criptoParser.CondParentContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", "( " + visit(ctx.cond()).render() + " )");
      return res;
   }

   @Override public ST visitCondNUM(criptoParser.CondNUMContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.NUM().getText());
      return res;
   }

   @Override public ST visitCondBIT(criptoParser.CondBITContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.BIT().getText() );
      return res;
   }

   @Override public ST visitCondOperators(criptoParser.CondOperatorsContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", "( " + visit(ctx.operators()).render() + " )");
      return res;
   }

   @Override public ST visitCondVarExpr(criptoParser.CondVarExprContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.ID().getText());
      res.add("arg", ".value");
      return res;
   }

   @Override public ST visitOperByte(criptoParser.OperByteContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.BYTE().getText());
      return res;
   }

   @Override public ST visitOperID(criptoParser.OperIDContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.ID().getText());
      return res;
   }

   @Override public ST visitOperBit(criptoParser.OperBitContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.BIT().getText());
      return res;
   }

   @Override public ST visitOperParentheses(criptoParser.OperParenthesesContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", "( " + visit(ctx.operators()).render() + " )");
      return res;
   }

   @Override public ST visitOperGetCommands(criptoParser.OperGetCommandsContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", visit(ctx.getCommands()).render());
      return res;
   }

   @Override public ST visitOperPow(criptoParser.OperPowContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg",visit(ctx.operators(0)).render() + ".pow(" + visit(ctx.operators(1)).render() + ")");
      return res;
   }

   @Override public ST visitOperNum(criptoParser.OperNumContext ctx) {
      ST res = templates.getInstanceOf("string");
      res.add("arg", ctx.NUM().getText());
      return res;
   }

   @Override public ST visitOperDivMultRest(criptoParser.OperDivMultRestContext ctx) {
      ST res = templates.getInstanceOf("string");
      String operator = "";
      switch(ctx.op.getText()) {
         case "*":
            operator = "multiply";
            break;
         case "\\":
            operator = "divide";
            break;
         case "%":
            operator = "rem";
            break;
      }
      res.add("arg",visit(ctx.operators(0)).render() + "." + operator + "(" + visit(ctx.operators(1)).render() + ")");
      return res;
   }

   @Override public ST visitOperAddSub(criptoParser.OperAddSubContext ctx) {
      ST res = templates.getInstanceOf("string");
      String operator = "";
      switch(ctx.op.getText()){
         case "+":
            operator = "add";
            break;
         case "-":
            operator = "subtract";
             break;
         case "xor":
            operator = "operation_xor";
            break;
         case "or":
            operator = "operation_or";
            break;
         case "and":
            operator = "operation_and";
            break;
      }
      res.add("arg",visit(ctx.operators(0)).render() + "." + operator + "(" + visit(ctx.operators(1)).render() + ")");
      return res;
   }
   
   @Override public ST visitWhileLoop(criptoParser.WhileLoopContext ctx) {
      ST res = templates.getInstanceOf("while");
      res.add("cond", visit(ctx.cond()).render());
      Iterator<criptoParser.LineContext> it = ctx.blockCode().line().iterator();
      while(it.hasNext()){
         res.add("body", visit(it.next()));
      }
      return res;
   }

   @Override public ST visitIfCond(criptoParser.IfCondContext ctx) {
      ST res = templates.getInstanceOf("if");
      res.add("cond", visit(ctx.cond()).render());
      Iterator<criptoParser.LineContext> it = ctx.blockCode().line().iterator();
      while(it.hasNext()){
         res.add("body", visit(it.next()));
      }
      return res;
   }

   @Override public ST visitForLoop(criptoParser.ForLoopContext ctx) {
      ST res = templates.getInstanceOf("forLoop");
      
      visit(ctx.forLeft());
      if(ctx.forLeft().var == null){
         res.add("var", "_");
      }else {
         res.add("var", ctx.forLeft().var);
      }

      res.add("min", ctx.forLeft().min);
      
      visit(ctx.forRight());
      res.add("max", ctx.forRight().n);
      
      Iterator<criptoParser.LineContext> it = ctx.blockCode().line().iterator();
      while(it.hasNext()){
         res.add("body", visit(it.next()));
      }
      return res;
   }

   @Override public ST visitForLDeclareOption(criptoParser. ForLDeclareOptionContext ctx) {
      ctx.var = ctx.declareFor().ID(0).getText();
      if(ctx.declareFor().NUM() != null){
         ctx.min = ctx.declareFor().NUM().getText(); 
      }else if(ctx.declareFor().ID(1) != null){
         ctx.min = ctx.declareFor().ID(1).getText() + ".value";
      }
      return null;
   }

   @Override public ST visitForAssign(criptoParser.ForAssignContext ctx) {
      ctx.var = ctx.assignmentFor().ID(0).getText();
      if(ctx.assignmentFor().NUM() != null){
         ctx.min = ctx.assignmentFor().NUM().getText();
      }else if(ctx.assignmentFor().ID(1) != null){
         ctx.min = ctx.assignmentFor().ID(1).getText()+ ".value";
      }
      return null;
   }

   @Override public ST visitForLVariable(criptoParser.ForLVariableContext ctx) {
      ctx.min = ctx.ID().getText()+ ".value";
     return null;
   }

   @Override public ST visitForLNumber(criptoParser.ForLNumberContext ctx) {
     ctx.min = ctx.NUM().getText();
     return null;
   }

   @Override public ST visitForRVariable(criptoParser.ForRVariableContext ctx) {
      ctx.n = ctx.ID().getText()+ ".value";
      return null;
   }

   @Override public ST visitForRNumber(criptoParser.ForRNumberContext ctx) {
      ctx.n = ctx.NUM().getText();
      return null;
   }

   @Override public ST visitAssignmentFor(criptoParser.AssignmentForContext ctx) {
      return visitChildren(ctx);
   }

   @Override public ST visitDeclareFor(criptoParser.DeclareForContext ctx) {
      return visitChildren(ctx);
   }

   public ST visitOPERATOR(String operator) {
      ST res = templates.getInstanceOf("string");
      switch(operator) {            
         case "&&":
            res.add("arg", "and");
            break;
            
         case "||":
            res.add("arg", "or");
            break;
         
         default:
            res.add("arg", operator);
            break;
      }
      return res;
   }

   public ST visitNUM(String value) {
      ST res = templates.getInstanceOf("num");
      int representation = 64;
      if(value.contains("0b")){
         representation = 0;
      }
      else if(value.contains("0x")) {
         representation = 8;
      }
      res.add("value", value.replace("0b", ""));
      res.add("representation", representation);
      return res;
   }

}