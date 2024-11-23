package parser.visitor;

import java.util.ArrayList;
import java.util.List;

import parser.ast.CommandWithArrays;
import parser.ast.Expression;
import parser.ast.ExpressionArrayIndexing;
import parser.ast.ExpressionIdent;
import parser.ast.RewardStructItem;
import parser.ast.UncertainUpdates;
import parser.ast.Update;
import parser.ast.UpdateElement;
import parser.ast.Updates;
import prism.PrismLangException;

public class ASTExpressionArrayIndexingVisitor extends ASTTraverse {

  private List<ExpressionArrayIndexing> arrayIndexingExpressions;

  public ASTExpressionArrayIndexingVisitor() {
    arrayIndexingExpressions = new ArrayList<ExpressionArrayIndexing>();
  }

  @Override
  public Object visit(RewardStructItem e) throws PrismLangException {
		e.getStates().accept(this);
		e.getReward().accept(this);

		return arrayIndexingExpressions;
	}

  @Override
	public Object visit(CommandWithArrays e) throws PrismLangException {
		Expression guard = e.getGuard();
    
    guard.accept(this);

    Updates updates = e.getUpdates();
    
    updates.accept(this);

		return arrayIndexingExpressions;
	}

  @Override
  public Object visit(UncertainUpdates e) throws PrismLangException {
		int i, n;
		n = e.getNumUpdates();

    Update update = null;
    Expression probability = null;

		for (i = 0; i < n; i++) {
      probability = e.getProbability(i);
      if (probability != null) {
        probability.accept(this);
      }

      update = e.getUpdate(i);
			if (update != null) {
        update.accept(this);
      }
		}

		return null;
	}

  @Override
  public Object visit(UpdateElement e) throws PrismLangException {
    if (e.getExpression() != null) {
      ExpressionIdent assigned = e.getVarIdent();
      assigned.accept(this);

      Expression expression = e.getExpression();
      expression.accept(this);
    }

    return null;
  }

  @Override
  public Object visit(ExpressionArrayIndexing e) throws PrismLangException {
    arrayIndexingExpressions.add(e);
    
    return null;
  }
}
