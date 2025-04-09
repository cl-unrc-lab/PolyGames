package parser.visitor.utils;

import java.util.HashMap;
import java.util.Map;

import parser.ast.*;
import parser.type.TypeInt;
import parser.visitor.DeepCopy;
import prism.PrismLangException;

public class CommandWithArraysReplacer extends ASTElementReplacer {
  public CommandWithArraysReplacer() {}

  @Override
  public ASTElement replace(ASTElement astElement, ExpressionArrayIndex expressionArrayIndex, Expression expression, int index)
      throws PrismLangException {
    setExpression(expression);
    setExpressionArrayIndex(expressionArrayIndex);

    Command command = (Command) astElement.clone().deepCopy(new DeepCopy()); // astElement.accept(new DeepCopy());

    command.setGuard(
      ExpressionBinaryOp.And(
        (Expression) command.getGuard().accept(this), // this replaces the ExpressionArrayIndex in the guard
        new ExpressionBinaryOp(5, expressionArrayIndex.index(), new ExpressionLiteral(TypeInt.getInstance(), index))
      )
    );

    command.setUpdates((Updates) command.getUpdates().accept(this)); // this replaces the ExpressionArrayIndex in the updates
    
    return command;
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
}
