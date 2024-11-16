package parser.visitor;

import java.util.ArrayList;
import java.util.List;

import parser.ast.CommandWithArrays;
import parser.ast.Expression;
import parser.ast.ExpressionArrayIndexing;
import parser.ast.ExpressionIdent;
import parser.ast.UpdateElement;
import parser.ast.Updates;
import prism.PrismLangException;

public class ASTExpressionArrayIndexingVisitor extends ASTTraverse {

  private List<ExpressionArrayIndexing> arrayIndexingExpressions;

  public ASTExpressionArrayIndexingVisitor() {
    arrayIndexingExpressions = new ArrayList<ExpressionArrayIndexing>();
  }

	public Object visit(CommandWithArrays e) throws PrismLangException {
		Expression guard = e.getGuard();
    
    guard.accept(this);

    Updates updates = e.getUpdates();
    
    updates.accept(this);

		return arrayIndexingExpressions;
	}

  public Object visit(UpdateElement e) throws PrismLangException {
    if (e.getExpression() != null) {
      ExpressionIdent assigned = e.getVarIdent();
      assigned.accept(this);

      Expression expression = e.getExpression();
      expression.accept(this);
    }

    return null;
  }

  public Object visit(ExpressionArrayIndexing e) throws PrismLangException {
    arrayIndexingExpressions.add(e);
    
    return null;
  }
}
