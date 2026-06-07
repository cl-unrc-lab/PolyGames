package parser.visitor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import parser.Values;
import parser.ast.*;
import parser.type.Type;
import prism.PrismLangException;

public class ConstantsReplacerVisitor extends ASTTraverseModify {
  private Values            constantValuesForUndefinedMFConstants;
  private ExpressionIdent   expressionIdent;
  private ExpressionLiteral value;

  private ModulesFile modulesFile;

  public ConstantsReplacerVisitor(Values constantValuesForUndefinedMFConstants) {
    this.constantValuesForUndefinedMFConstants = constantValuesForUndefinedMFConstants;
  }

  @Override
  public Object visit(ModulesFile e) throws PrismLangException {
    this.modulesFile = e;

    return super.visit(e);
  }

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
      Command command = e.getCommand(0);

      e.removeCommand(command);

			if (command != null) {
        e.addCommand((Command) command.accept(this));
      }
		}

		return e;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(CommandWithArrays e) throws PrismLangException {
    ASTVisitor searcher = new ASTElementSearcherVisitor(ExpressionIdent.class);
    List<ExpressionIdent> expressionConstantList = (List<ExpressionIdent>) e.accept(searcher);

    for (ExpressionIdent expression : expressionConstantList) {
      this.expressionIdent = expression;
      this.value           = resolve(expression);

      if (value == null) {
        continue ;
      }

      Expression guard = e.getGuard();
      guard.accept(this);

      Updates updates = e.getUpdates();
      updates.accept(this);
    }
    

    return e;
  }

  private ExpressionLiteral resolve(Expression expression) throws PrismLangException {
    String identifier = null;

    if (expression instanceof ExpressionIdent) {
      identifier = ((ExpressionIdent)    expression).getName();
    } else if (expression instanceof ExpressionConstant) {
      identifier = ((ExpressionConstant) expression).getName();
    } else {
      return null;
    }

    int indexOfConstant = constantValuesForUndefinedMFConstants.getIndexOf(identifier);

    if (indexOfConstant != -1) {
      Type type    = constantValuesForUndefinedMFConstants.getType(indexOfConstant);
      Object value = constantValuesForUndefinedMFConstants.getValue(indexOfConstant);

      return new ExpressionLiteral(type, value);
    }

    ConstantList constantList = modulesFile.getConstantList();
    indexOfConstant = constantList.getConstantIndex(identifier);

    if (indexOfConstant != -1) {
      return new ExpressionLiteral(
        constantList.getConstantType(indexOfConstant), constantList.getConstant(indexOfConstant).evaluate()
      );
    }

    return null;
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
  public Object visit(DeclarationInt e) throws PrismLangException {
    Expression low  = e.getLow();
    Expression high = e.getHigh();

    if (low != null) {
      if (low instanceof ExpressionConstant) {
        e.setLow(resolve(low));
      } else {
        e.setLow(
          (Expression) low.accept(this)
        );
      }
    }

		if (high != null) {
      if (high instanceof ExpressionConstant) {
        e.setHigh(resolve(high));
      } else {
        e.setHigh(
          (Expression) high.accept(this)
        );
      }
    }

    return e;
  }

  @Override
  public Object visit(ExpressionIdent e) throws PrismLangException {
    if (expressionIdent != null && (e.getName().equals(expressionIdent.getName()))) {
      return value;
    } else {
      return e;
    }
  }
}