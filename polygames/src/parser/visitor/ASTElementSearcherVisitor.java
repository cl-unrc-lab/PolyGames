package parser.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parser.ast.*;
import prism.PrismLangException;

public class ASTElementSearcherVisitor implements ASTVisitor {
  private Class<? extends ASTElement> classType;
  private List<ASTElement> elements;

  public ASTElementSearcherVisitor(Class<? extends ASTElement> classType) {
    this.classType = classType;
    this.elements  = new ArrayList<ASTElement>();
  }

  @Override
	public Object visit(ModulesFile e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		if (e.getFormulaList() != null) {
      e.getFormulaList().accept(this);
    }
    
		if (e.getLabelList() != null) {
      e.getLabelList().accept(this);
    }

		if (e.getConstantList() != null) {
      e.getConstantList().accept(this);
    }

		n = e.getNumGlobals();
		for (i = 0; i < n; i++) {
			if (e.getGlobal(i) != null) {
        e.getGlobal(i).accept(this);
      }
    }
    
		n = e.getNumModules();
		for (i = 0; i < n; i++) {
			if (e.getModule(i) != null) {
        e.getModule(i).accept(this);
      }
		}

		if (e.getSystemDefn() != null) {
      e.getSystemDefn().accept(this);
    }

		n = e.getNumRewardStructs();
		for (i = 0; i < n; i++) {
			if (e.getRewardStruct(i) != null) {
        e.getRewardStruct(i).accept(this);
      }
		}

		if (e.getInitialStates() != null) {
      e.getInitialStates().accept(this);
    }
    
		n = e.getNumObservableVarLists();
		for (i = 0; i < n; i++) {
			if (e.getObservableVarList(i) != null) {
        e.getObservableVarList(i).accept(this);
      }
		}

		n = e.getNumObservableDefinitions();
		for (i = 0; i < n; i++) {
			if (e.getObservableDefinition(i) != null) {
        e.getObservableDefinition(i).accept(this);
      }
		}

		n = e.getNumPlayers();
		for (i = 0; i < n; i++) {
			if (e.getPlayer(i) != null) {
        e.getPlayer(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(PropertiesFile e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		if (e.getLabelList() != null) {
      e.getLabelList().accept(this);
    }

		if (e.getConstantList() != null) {
      e.getConstantList().accept(this);
    }

		n = e.getNumProperties();
		for (i = 0; i < n; i++) {
			if (e.getPropertyObject(i) != null) {
        e.getPropertyObject(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(Property e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(FormulaList e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.size();
		for (i = 0; i < n; i++) {
      Expression formula = e.getFormula(i);

			if (formula != null) {
        formula.accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(LabelList e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.size();
		for (i = 0; i < n; i++) {
			if (e.getLabel(i) != null) {
        e.getLabel(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(ConstantList e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.size();
		for (i = 0; i < n; i++) {
			if (e.getConstant(i) != null) {
        e.getConstant(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(Declaration e) throws PrismLangException {
    addASTElementIfIsInstance(e);

    if (e.getDeclType() != null) {
      e.getDeclType().accept(this);
    }

    if (e.getStart() != null) {
      e.getStart().accept(this);
    }

		return elements;
	}

	@Override
	public Object visit(DeclarationInt e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getLow() != null) {
      e.getLow().accept(this);
    }

		if (e.getHigh() != null) {
      e.getHigh().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(DeclarationBool e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(DeclarationArray e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getLow() != null) {
      e.getLow().accept(this);
    }

		if (e.getHigh() != null) {
      e.getHigh().accept(this);
    }

		if (e.getSubtype() != null) {
      e.getSubtype().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(DeclarationClock e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(DeclarationIntUnbounded e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(parser.ast.Module e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.getNumDeclarations();
		for (i = 0; i < n; i++) {
			if (e.getDeclaration(i) != null) {
        e.getDeclaration(i).accept(this);
      }
		}

		if (e.getInvariant() != null) {
      e.getInvariant().accept(this);
    }
    
		n = e.getNumCommands();
		for (i = 0; i < n; i++) {
			if (e.getCommand(i) != null) {
        e.getCommand(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(Command e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		Expression guard = e.getGuard();
    guard.accept(this);

		Updates updates = e.getUpdates();
    updates.accept(this);

		return elements;
	}

  @Override
	public Object visit(Updates e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.getNumUpdates();
		for (i = 0; i < n; i++) {
			if (e.getProbability(i) != null) {
        e.getProbability(i).accept(this);
      }

			if (e.getUpdate(i) != null) {
        e.getUpdate(i).accept(this);
      }
		}

		return elements;
	}	

  @Override
	public Object visit(UncertainUpdates e) throws PrismLangException {
    addASTElementIfIsInstance(e);

    int i, n;
    Expression coefficient;

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

    for (Map.Entry<String, HashMap<Integer, Expression>> entry : e.coefficients().entrySet()) {
      for (Map.Entry<Integer, Expression> row : entry.getValue().entrySet()) {
        coefficient = row.getValue();

        coefficient.accept(this);
      }
    }

    for (Expression constant : e.constants()) {
      constant.accept(this);
    }

    return elements;
	}

  @Override
	public Object visit(Update e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.getNumElements();
		for (i = 0; i < n; i++) {
			if (e.getElement(i) != null) {
        e.getElement(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(UpdateElement e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getVarIdent() != null) {
      ExpressionIdent identifier = e.getVarIdent();
      identifier.accept(this);
    }

    if (e.getExpression() != null) {
      Expression expression = e.getExpression();
      expression.accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(RenamedModule e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(RewardStruct e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.getNumItems();
		for (i = 0; i < n; i++) {
			if (e.getRewardStructItem(i) != null) {
        e.getRewardStructItem(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(RewardStructItem e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getStates().accept(this);
		e.getReward().accept(this);

		return elements;
	}

  @Override
	public Object visit(ObservableVars e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n = e.getNumVars();
		for (i = 0; i < n; i++) {
			if (e.getVar(i) != null) {
        e.getVar(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(Observable e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getDefinition() != null) {
      e.getDefinition().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(Player e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(SystemInterleaved e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n = e.getNumOperands();
		for (i = 0; i < n; i++) {
			if (e.getOperand(i) != null) {
        e.getOperand(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(SystemFullParallel e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n = e.getNumOperands();
		for (i = 0; i < n; i++) {
			if (e.getOperand(i) != null) {
        e.getOperand(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(SystemParallel e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getOperand1().accept(this);
		e.getOperand2().accept(this);

		return elements;
	}

  @Override
	public Object visit(SystemHide e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getOperand().accept(this);

		return elements;
	}

  @Override
	public Object visit(SystemRename e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getOperand().accept(this);

		return elements;
	}

  @Override
	public Object visit(SystemModule e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(SystemBrackets e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getOperand().accept(this);
		return elements;
	}

  @Override
	public Object visit(SystemReference e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}
	
  @Override
	public Object visit(ExpressionTemporal e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getOperand1() != null) {
      e.getOperand1().accept(this);
    }

		if (e.getOperand2() != null) {
      e.getOperand2().accept(this);
    }

		if (e.getLowerBound() != null) {
      e.getLowerBound().accept(this);
    }

		if (e.getUpperBound() != null) {
      e.getUpperBound().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionITE e) throws PrismLangException {
    addASTElementIfIsInstance(e);
    
		e.getOperand1().accept(this);
		e.getOperand2().accept(this);
		e.getOperand3().accept(this);

		return elements;
	}

  @Override
	public Object visit(ExpressionBinaryOp e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getOperand1().accept(this);
		e.getOperand2().accept(this);

		return elements;
	}

  @Override
	public Object visit(ExpressionUnaryOp e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		e.getOperand().accept(this);

		return elements;
	}

  @Override
	public Object visit(ExpressionFunc e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n = e.getNumOperands();
		for (i = 0; i < n; i++) {
			if (e.getOperand(i) != null) {
        e.getOperand(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(ExpressionIdent e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ExpressionLiteral e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ExpressionConstant e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ExpressionFormula e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getDefinition() != null) e.getDefinition().accept(this);

		return elements;
	}

  @Override
	public Object visit(ExpressionVar e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ExpressionInterval e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getOperand1() != null) {
      e.getOperand1().accept(this);
    }

		if (e.getOperand2() != null) {
      e.getOperand2().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionProb e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getProb() != null) {
      e.getProb().accept(this);
    }

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		if (e.getFilter() != null) {
      e.getFilter().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionReward e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getRewardStructIndex() != null && e.getRewardStructIndex() instanceof Expression) {
      ((Expression) e.getRewardStructIndex()).accept(this);
    }

		if (e.getRewardStructIndexDiv() != null && e.getRewardStructIndexDiv() instanceof Expression) {
      ((Expression) e.getRewardStructIndexDiv()).accept(this);
    }

		if (e.getReward() != null) {
      e.getReward().accept(this);
    }

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		if (e.getFilter() != null) {
      e.getFilter().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionMultiNash e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getBound() != null) {
      e.getBound().accept(this);
    }

		int i, n = e.getNumOperands();
		for (i = 0; i < n; i++) {
			if (e.getOperand(i) != null) {
        e.getOperand(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(ExpressionMultiNashProb e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getExpression() != null) {
      ((Expression) e.getExpression()).accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionMultiNashReward e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getExpression() != null) {
      ((Expression) e.getExpression()).accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionSS e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getProb() != null) {
      e.getProb().accept(this);
    }

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		if (e.getFilter() != null) {
      e.getFilter().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionExists e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionForAll e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(ExpressionStrategy e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n = e.getNumOperands();
		for (i = 0; i < n; i++) {
			if (e.getOperand(i) != null) {
        e.getOperand(i).accept(this);
      }
		}

		return elements;
	}

  @Override
	public Object visit(ExpressionLabel e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

	public Object visit(ExpressionObs e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ExpressionProp e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ExpressionFilter e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getFilter() != null) {
      e.getFilter().accept(this);
    }

		if (e.getOperand() != null) {
      e.getOperand().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(Filter e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getExpression() != null) {
      e.getExpression().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(UncertainExpression e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		return elements;
	}

  @Override
	public Object visit(ForLoop e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		if (e.getFrom() != null) {
      e.getFrom().accept(this);
    }

		if (e.getTo() != null) {
      e.getTo().accept(this);
    }

		if (e.getStep() != null) {
      e.getStep().accept(this);
    }

		return elements;
	}

  @Override
	public Object visit(CommandWithArrays e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		Expression guard = e.getGuard();
    guard.accept(this);

    Updates updates = e.getUpdates();
    updates.accept(this);

		return elements;
	}
	
	@Override
  public Object visit(ExpressionArrayIndex e) throws PrismLangException {
    addASTElementIfIsInstance(e);
    
    return elements;
  }

  @Override
  public Object visit(RewardStructWithArrays e) throws PrismLangException {
    addASTElementIfIsInstance(e);

		int i, n;
		n = e.getNumItems();
		for (i = 0; i < n; i++) {
			if (e.getRewardStructItem(i) != null) {
        e.getRewardStructItem(i).accept(this);
      }
		}

		return elements;
  }

	private void addASTElementIfIsInstance(ASTElement e) {
    if (e.getClass() == this.classType) {
      this.elements.add(e);
    }
  }
}
