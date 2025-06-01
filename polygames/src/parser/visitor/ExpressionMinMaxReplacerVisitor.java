package parser.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.ast.*;
import parser.type.TypeDouble;
import prism.PrismLangException;

public class ExpressionMinMaxReplacerVisitor extends ASTTraverseModify {
  private ExpressionMinMax expressionMinMax;
  private Expression value;

  @Override
  public Object visit(parser.ast.Module e) throws PrismLangException {
		int i, n;
		n = e.getNumDeclarations();
		for (i = 0; i < n; i++) {
			if (e.getDeclaration(i) != null) {
        e.setDeclaration(i, (Declaration) (e.getDeclaration(i).accept(this)));
      }
		}

		if (e.getInvariant() != null) {
      e.setInvariant((Expression) (e.getInvariant().accept(this)));
    }

    n = e.getNumCommands();
		for (i = 0; i < n; i++) {
      Command command = e.getCommand(i);

      e.removeCommand(command);

			if (command != null) {
        e.addCommand((Command) command.accept(this));
      }
		}

		return e;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(Command e) throws PrismLangException {
    ASTVisitor searcher = new ASTElementSearcherVisitor(ExpressionMinMax.class);
    List<ExpressionMinMax> expressionMinMaxList = (List<ExpressionMinMax>) e.getUpdates().accept(searcher);

    for (ExpressionMinMax expressionMinMax : expressionMinMaxList) {
      this.expressionMinMax = expressionMinMax;
      double left  = expressionMinMax.left().evaluateDouble();
      double right = expressionMinMax.right().evaluateDouble();
      double value = expressionMinMax.function() == ExpressionMinMax.MAX ? Math.max(left, right) : Math.min(left, right);
      this.value   = new ExpressionLiteral(TypeDouble.getInstance(), value);

      e.setUpdates((Updates) e.getUpdates().accept(this));
    }

    return e;
  }

  @Override
  public Object visit(UncertainUpdates e) throws PrismLangException {
    int i, n;
    String uncertain;
    Expression coefficient;
    int index;

		n = e.getNumUpdates();

    Expression probability = null;
    Update update = null;

		for (i = 0; i < n; i++) {
      probability = e.getProbability(i);
			if (probability != null) {
        e.setProbability(i, (Expression) (probability.accept(this)));
      }

      update = e.getUpdate(i);
			if (update != null) {
        e.setUpdate(i, (Update) (update.accept(this)));
      }
		}

    for (Map.Entry<String, HashMap<Integer, Expression>> entry : e.coefficients().entrySet()) {
      uncertain = entry.getKey();

      for (Map.Entry<Integer, Expression> row : entry.getValue().entrySet()) {
        index       = row.getKey();
        coefficient = row.getValue();

        e.setCoefficient(uncertain, index, (Expression) coefficient.accept(this));
      }
    }

    for (i = 0; i < e.constants().size(); i++) {
      Expression constant = e.constant(i);

      e.setConstant(i, (Expression) constant.accept(this));
    }

		return e;
	}

  @Override
  public Object visit(UpdateElement e) throws PrismLangException {
    ExpressionIdent identifier = e.getVarIdent();
    if (identifier != null) {
      e.setVarIdent((ExpressionIdent) identifier.accept(this));
    }

    Expression expression = e.getExpression();
    if (expression != null) {
      e.setExpression((Expression) expression.accept(this));;
    }

    return e;
  }

  @Override
  public Object visit(ExpressionMinMax e) throws PrismLangException {
    if (e == expressionMinMax) {
      return value;
    } else {
      return e;
    }
  }
}