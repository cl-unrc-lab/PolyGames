package parser.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import parser.ast.Command;
import parser.ast.CommandWithArrays;
import parser.ast.Declaration;
import parser.ast.Expression;
import parser.ast.ExpressionArrayIndexing;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionLiteral;
import parser.ast.Module;
import parser.ast.UpdateElement;
import parser.ast.Updates;
import prism.PrismLangException;

public class ASTCommandWithArraysVisitor extends ASTTraverseModify {

  private Module module;
  private ExpressionArrayIndexing replaced;
  private ExpressionIdent         replacer;

  @Override
  public Object visit(parser.ast.Module e) throws PrismLangException {
		int i, n;
		n = e.getNumDeclarations();
		for (i = 0; i < n; i++) {
			if (e.getDeclaration(i) != null) e.setDeclaration(i, (Declaration) (e.getDeclaration(i).accept(this)));
		}

		if (e.getInvariant() != null)
			e.setInvariant((Expression) (e.getInvariant().accept(this)));

    n = e.getNumCommands();
		for (i = 0; i < n; i++) {
      Command command = e.getCommand(0);

			if (command != null) {
        command.accept(this);

        e.removeCommand(command);
      }
		}

		return e;
	}
        
  @SuppressWarnings("unchecked")
  @Override
  public Object visit(CommandWithArrays e) throws PrismLangException {
    this.module = e.getParent();

    ASTExpressionArrayIndexingVisitor checker = new ASTExpressionArrayIndexingVisitor();

    // A CommandWithArray may have many ExpressionArrayIndexing
    List<ExpressionArrayIndexing> arrayIndexingExpressions = (List<ExpressionArrayIndexing>) checker.visit(e);
    if ( !arrayIndexingExpressions.isEmpty() ) { // Does it contain array indexing expressions?
      // If so, we need to replace this CommandWithArray with a set of Commands.

      /**
       * 
       * [] guard -> (x'=array[i]) + (y'=array[j]) + (z'=array[k]); // 3 references to ExpressionArrayIndexing, let's assume that array is declared as array[3] : [0..3];
       * =>
       * [] guard & i = 0 -> (x'=array0) + (y'=array[j]) + (z'=array[k]);
       * [] guard & i = 1 -> (x'=array1) + (y'=array[j]) + (z'=array[k]);
       * [] guard & i = 2 -> (x'=array2) + (y'=array[j]) + (z'=array[k]);
       * => { let's continue with the next reference to ExpressionArrayIndexing }
       * [] guard & i = 0 & j = 0 -> (x'=array0) + (y'=array0) + (z'=array[k]);
       * [] guard & i = 0 & j = 1 -> (x'=array0) + (y'=array1) + (z'=array[k]);
       * [] guard & i = 0 & j = 2 -> (x'=array0) + (y'=array2) + (z'=array[k]);
       * [] guard & i = 1 & j = 0 -> (x'=array1) + (y'=array0) + (z'=array[k]);
       * [] guard & i = 1 & j = 1 -> (x'=array1) + (y'=array1) + (z'=array[k]);
       * [] guard & i = 1 & j = 2 -> (x'=array1) + (y'=array2) + (z'=array[k]);
       * [] guard & i = 2 & j = 0 -> (x'=array2) + (y'=array0) + (z'=array[k]);
       * [] guard & i = 2 & j = 1 -> (x'=array2) + (y'=array1) + (z'=array[k]);
       * [] guard & i = 2 & j = 2 -> (x'=array2) + (y'=array2) + (z'=array[k]);
       * => { let's continue with the next reference to ExpressionArrayIndexing }
       * ...
       * 
       * once finished with all the references we add those commands to the module.
       */

      List<Command> commands = new ArrayList<>();

      for (ExpressionArrayIndexing expressionArrayIndexing : arrayIndexingExpressions) {
        List<Declaration> declarations =
          module.getDeclarations().stream()
                                  .filter(declaration -> declaration.getName()
                                  .startsWith(expressionArrayIndexing.name()))
                                  .collect(Collectors.toList());
        
        ListIterator<Command> iterator = commands.listIterator();
        if (commands.isEmpty()) {
          replaceArrayIndexingExpressions(declarations, expressionArrayIndexing, e, iterator);
        } else {
          while (iterator.hasNext()) {
            Command command = iterator.next();
            iterator.remove();

            replaceArrayIndexingExpressions(declarations, expressionArrayIndexing, command, iterator);
          }
        }
      }

      for (Command command : commands)
        module.addCommand(command);

    } else {
      // Otherwise, we need to replace this CommandWithArray with a single Command.
      Command command = e.ConvertToCommand();

      module.addCommand(command);
    }

    return null;
  }

  private void replaceArrayIndexingExpressions(List<Declaration> declarations, ExpressionArrayIndexing expressionArrayIndexing, Command command, ListIterator<Command> iterator) throws PrismLangException {
    int position = 0;

    for (Declaration declaration : declarations) {
      this.replaced = expressionArrayIndexing;
      this.replacer = new ExpressionIdent(declaration.getName());

      Command c = commandBuilder(command, expressionArrayIndexing, declaration, position);

      iterator.add(c);
      position += 1;
    }
  }

  private Command commandBuilder(Command command, ExpressionArrayIndexing expr, Declaration declaration, int position) throws PrismLangException {
    Command c = new Command();

    c.setGuard(
      // guard & index = position
      ExpressionBinaryOp.And(
        (Expression) command.getGuard().clone().accept(this),
        new ExpressionBinaryOp(5, expr.index(), new ExpressionLiteral(declaration.getType(), position))
      )
    );

    c.setUpdates((Updates) command.getUpdates().clone().accept(this));

    c.setSynchs(command.getSynchs());

    return c;
  }

  @Override
  public Object visit(UpdateElement e) throws PrismLangException {
    ExpressionIdent assigned = e.getVarIdent();
    if (assigned != null) {
      e.setVarIdent((ExpressionIdent) assigned.accept(this));
    }

    Expression expression = e.getExpression();
    if (expression != null) {
      e.setExpression((Expression) expression.accept(this));;
    }

    return e;
  }

  @Override
  public Object visit(ExpressionArrayIndexing e) throws PrismLangException {
    if (e == replaced) {
      return replacer;
    } else {
      return e;
    }
  }
}
