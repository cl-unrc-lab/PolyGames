package parser.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import parser.ast.Constant;
import parser.ast.Command;
import parser.ast.CommandWithArrays;
import parser.ast.ConstantList;
import parser.ast.Declaration;
import parser.ast.Expression;
import parser.ast.ExpressionArrayIndexing;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionLiteral;
import parser.ast.FormulaList;
import parser.ast.LabelList;
import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.Observable;
import parser.ast.ObservableVars;
import parser.ast.Player;
import parser.ast.RewardStruct;
import parser.ast.RewardStructItem;
import parser.ast.RewardStructWithArrays;
import parser.ast.SystemDefn;
import parser.ast.UncertainUpdates;
import parser.ast.Update;
import parser.ast.UpdateElement;
import parser.ast.Updates;
import parser.type.TypeInt;
import prism.PrismLangException;

public class ASTWithArraysVisitor extends ASTTraverseModify {
  private ModulesFile modulesFile;
  private RewardStructWithArrays rewardStruct;
  private ExpressionArrayIndexing replaced;
  private Expression replacement;

  private enum MatchingType   { EXACTLY, STARTS_WITH };
  private enum EvaluationType { EXACTLY, COMBINATION };

  @Override
  public Object visit(ModulesFile e) throws PrismLangException {
    this.modulesFile = e;

		int i, n;
		if (e.getFormulaList() != null) e.setFormulaList((FormulaList)(e.getFormulaList().accept(this)));
		if (e.getLabelList() != null) e.setLabelList((LabelList)(e.getLabelList().accept(this)));
		if (e.getConstantList() != null) e.setConstantList((ConstantList)(e.getConstantList().accept(this)));
		n = e.getNumGlobals();
		for (i = 0; i < n; i++) {
			if (e.getGlobal(i) != null) e.setGlobal(i, (Declaration)(e.getGlobal(i).accept(this)));
		}
		n = e.getNumModules();
		for (i = 0; i < n; i++) {
			if (e.getModule(i) != null) e.setModule(i, (parser.ast.Module)(e.getModule(i).accept(this)));
		}
		n = e.getNumSystemDefns();
		for (i = 0; i < n; i++) {
			if (e.getSystemDefn(i) != null) e.setSystemDefn(i, (SystemDefn)(e.getSystemDefn(i).accept(this)), e.getSystemDefnName(i));
		}
		n = e.getNumRewardStructs();
		for (i = 0; i < n; i++) {
			if (e.getRewardStruct(i) != null) e.setRewardStruct(i, (RewardStruct)(e.getRewardStruct(i).accept(this)));
		}
		if (e.getInitialStates() != null) e.setInitialStates((Expression)(e.getInitialStates().accept(this)));
		n = e.getNumObservableVarLists();
		for (i = 0; i < n; i++) {
			if (e.getObservableVarList(i) != null) e.setObservableVarList(i, (ObservableVars) e.getObservableVarList(i).accept(this));
		}
		n = e.getNumObservableDefinitions();
		for (i = 0; i < n; i++) {
			if (e.getObservableDefinition(i) != null) e.setObservableDefinition(i, (Observable) e.getObservableDefinition(i).accept(this));
		}
		n = e.getNumPlayers();
		for (i = 0; i < n; i++) {
			if (e.getPlayer(i) != null) e.setPlayer(i, (Player)(e.getPlayer(i).accept(this)));
		}
    
		return e;
	}

  @Override
  public Object visit(RewardStructWithArrays e) throws PrismLangException {
    this.rewardStruct = e;

		int i, n;
		n = e.getNumItems();
		for (i = 0; i < n; i++) {
      RewardStructItem rewardStructItem = e.getRewardStructItem(0);
			if (rewardStructItem != null) {
        rewardStructItem.accept(this);
      }
		}

    RewardStruct rewardStruct = new RewardStruct();
    rewardStruct.setName(e.getName());
		n = e.getNumItems();
    for (i = 0; i < n; i++) {
			if (e.getRewardStructItem(i) != null) {
        rewardStruct.addItem(e.getRewardStructItem(i));
      }
		}

		return rewardStruct;
	}

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(RewardStructItem e) throws PrismLangException {
    this.rewardStruct.removeItem(e);

    ASTExpressionArrayIndexingChecker checker = new ASTExpressionArrayIndexingChecker();
    List<ExpressionArrayIndexing> arrayIndexingExpressions = (List<ExpressionArrayIndexing>) checker.visit(e);

    if (!arrayIndexingExpressions.isEmpty()) {
      List<Object> rewardStructItems = new ArrayList<>();
      rewardStructItems.add(e);

      for (ExpressionArrayIndexing expression : arrayIndexingExpressions) {
        // Try to evaluate the ExpressionArrayIndexing
        try {
          int index = expression.index().evaluateInt();
          rewardStructItems =
            createASTElementsWithoutExpressionArrayIndexing(rewardStructItems, expression, matchingIdentifiers(expression.name() + index, MatchingType.EXACTLY), EvaluationType.EXACTLY);
        } catch (Exception exception) {
          rewardStructItems =
            createASTElementsWithoutExpressionArrayIndexing(rewardStructItems, expression, matchingIdentifiers(expression.name(), MatchingType.STARTS_WITH), EvaluationType.COMBINATION);
        }
      }

      for (Object rewardStructItem : rewardStructItems)
        rewardStruct.addItem((RewardStructItem) rewardStructItem);

    } else {
      e.setStates((Expression)(e.getStates().accept(this)));
      e.setReward((Expression)(e.getReward().accept(this)));
      rewardStruct.addItem(e);
    }

    return null;
	}

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
    Module module = e.getParent();

    ASTExpressionArrayIndexingChecker checker = new ASTExpressionArrayIndexingChecker();

    // A CommandWithArray may have many ExpressionArrayIndexing
    List<ExpressionArrayIndexing> arrayIndexingExpressions = (List<ExpressionArrayIndexing>) checker.visit(e);

    if ( !arrayIndexingExpressions.isEmpty() ) { // Does it contain array indexing expressions?
      // If so, we need to replace this CommandWithArray with a set of Commands.

      List<Object> commands = new ArrayList<>();
      commands.add(e);

      for (ExpressionArrayIndexing expression : arrayIndexingExpressions) {
        // Try to evaluate the ExpressionArrayIndexing
        try {
          int index = expression.index().evaluateInt();
          commands  =
            createASTElementsWithoutExpressionArrayIndexing(commands, expression, matchingIdentifiers(expression.name() + index, MatchingType.EXACTLY), EvaluationType.EXACTLY);
        } catch (Exception exception) {
          // First we replace the ExpressionArrayIndexing using the declarations
          commands =
            createASTElementsWithoutExpressionArrayIndexing(commands, expression, matchingIdentifiers(expression.name(), MatchingType.STARTS_WITH), EvaluationType.COMBINATION);
        }
      }

      for (Object command : commands)
        module.addCommand((Command) command);

    } else {
      // Otherwise, we need to replace this CommandWithArray with Command
      Command command = new Command();

      command.setGuard(e.getGuard());
      command.setSynchs(e.getSynchs());
      command.setUpdates(e.getUpdates());

      module.addCommand(command);
    }

    return null;
  }

  private List<Object> createASTElementsWithoutExpressionArrayIndexing(List<Object> elements, ExpressionArrayIndexing expression, List<Object> identifiers, EvaluationType evaluationType) throws PrismLangException {
    ListIterator<Object> iterator = elements.listIterator();

    while(iterator.hasNext()) {
      Object element = iterator.next();

      if ( !identifiers.isEmpty() )
        iterator.remove();

      int position = 0;
      if (evaluationType.equals(EvaluationType.EXACTLY) && identifiers.size() == 1)
        position = expression.index().evaluateInt();

      for (Object identifier : identifiers) {
        iterator.add(createASTElement(element, expression, identifier, position));

        position++;
      }
    }

    return elements;
  }

  private Object createASTElement(Object element, ExpressionArrayIndexing expression, Object identifier, int position) throws PrismLangException {
    if (element.getClass() == Command.class)
      return createASTElement((Command) element, expression, identifier, position);
    
    if (element.getClass() == CommandWithArrays.class)
      return createASTElement((CommandWithArrays) element, expression, identifier, position);

    if (element.getClass() == RewardStructItem.class)
      return createASTElement((RewardStructItem) element, expression, identifier, position);

    return null;
  }

  private Object createASTElement(Command command, ExpressionArrayIndexing expression, Object identifier, int position) throws PrismLangException {
    this.replaced    = expression;
    this.replacement = createReplacement(identifier, expression);

    Command c = new Command();
    c.setGuard(
      // guard & index = position
      ExpressionBinaryOp.And(
        (Expression) command.getGuard().clone().accept(this), // this replaces the expression array indexing in the guard
        new ExpressionBinaryOp(5, expression.index(), createExpressionLiteral(identifier, position))
      )
    );
    c.setUpdates((Updates) command.getUpdates().clone().accept(this)); // this replaces the expression array indexing in the updates
    c.setSynchs(command.getSynchs());
    
    return c;
  }

  private Object createASTElement(RewardStructItem rewardStructItem, ExpressionArrayIndexing expression, Object identifier, int position) throws PrismLangException {
    this.replaced    = expression;
    this.replacement = createReplacement(identifier, expression);

    Expression guard =
      ExpressionBinaryOp.And(
        (Expression) rewardStructItem.getStates().clone().accept(this), new ExpressionBinaryOp(5, expression.index(), createExpressionLiteral(identifier, position))
      );

    RewardStructItem rsi = new RewardStructItem(rewardStructItem.getSynchs(), guard, (Expression) rewardStructItem.getReward().accept(this));
    
    return rsi;
  }

  private Expression createReplacement(Object object, ExpressionArrayIndexing expression) throws PrismLangException {
    if (object.getClass() == Declaration.class) {
      Declaration declaration = (Declaration) object;

      ExpressionIdent result = new ExpressionIdent(declaration.getName());
      result.setPrime(expression.prime());

      return result;
    }

    if (object.getClass() == Constant.class) {
      Constant constant = (Constant) object;

      return new ExpressionLiteral(constant.type(), constant.evaluate());
    }

    return null;
  }

  private ExpressionLiteral createExpressionLiteral(Object object, int position) {
    return new ExpressionLiteral(TypeInt.getInstance(), position);
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

    index = 0;
    for (Expression constant : e.constants()) {
      e.setConstant(index, (Expression) constant.accept(this));

      index++;
    }

		return e;
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
      return replacement;
    } else {
      return e;
    }
  }

  private boolean matchStrings(String s1, String s2, MatchingType matchingType) {
    switch (matchingType) {
      case EXACTLY:
        return s1.equals(s2);
      default:
        return s1.startsWith(s2);
    }
  }

  private List<Object> matchingDeclarations(String prefix, MatchingType matchingType) {
    List<Object> matchingDeclarations = new ArrayList<>();

    int n = this.modulesFile.getNumModules();
    for (int i = 0; i < n; i++) {
      Module module = this.modulesFile.getModule(i);
      matchingDeclarations.addAll(
        module.getDeclarations().stream()
                                .filter(declaration -> matchStrings(declaration.getName(), prefix, matchingType))
                                .collect(Collectors.toList())
      );
    }

    return matchingDeclarations;
  }

  private List<Object> matchingConstants(String prefix, MatchingType matchingType) {
    List<Object> matchingConstants = new ArrayList<>();

    ConstantList constantList = this.modulesFile.getConstantList();

    int n = constantList.size();
    for (int i = 0; i < n; i++) {
      if (Objects.nonNull(constantList.getConstantName(i)) && !matchStrings(constantList.getConstantName(i), prefix, matchingType))
        continue;

      Constant constant =
        new Constant(constantList.getConstantName(i), constantList.getConstant(i), constantList.getConstantType(i));

      matchingConstants.add(constant);
    }

    return matchingConstants;
  }

  private List<Object> matchingIdentifiers(String prefix, MatchingType matchingType) {
    List<Object> identifiers = new ArrayList<>();

    identifiers.addAll(matchingDeclarations(prefix, matchingType));
    identifiers.addAll(matchingConstants(prefix, matchingType));

    return identifiers;
  }
}
