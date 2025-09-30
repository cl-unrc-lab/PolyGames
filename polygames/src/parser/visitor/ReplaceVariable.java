package parser.visitor;

import parser.ast.Module;
import parser.ast.ModulesFile;
import parser.ast.RewardStructWithArrays;
import parser.ast.UncertainExpression;
import prism.PrismLangException;
import parser.ast.CommandWithArrays;
import parser.ast.Expression;
import parser.ast.ExpressionArray;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionConstant;
import parser.ast.ExpressionExists;
import parser.ast.ExpressionFilter;
import parser.ast.ExpressionForAll;
import parser.ast.ExpressionFormula;
import parser.ast.ExpressionFunc;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionInterval;
import parser.ast.ExpressionLabel;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionMinMax;
import parser.ast.ExpressionMultiNash;
import parser.ast.ExpressionMultiNashProb;
import parser.ast.ExpressionMultiNashReward;
import parser.ast.ExpressionObs;
import parser.ast.ExpressionProb;
import parser.ast.ExpressionProp;
import parser.ast.ExpressionReward;
import parser.ast.ExpressionSS;
import parser.ast.ExpressionStrategy;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ExpressionVar;
import parser.ast.Filter;
import parser.ast.ForLoop;

/**
 * Replaces the occurrence of a variable by a given literal, this should be only applied to expressions, otherwise it only copies the structure
 * @author pablo
 *
 */
public class ReplaceVariable extends DeepCopy{
	private String vname;
	private ExpressionLiteral literal;
	
	public ReplaceVariable(String vname, ExpressionLiteral literal)
	{
		this.vname = vname;
		this.literal = literal;
	}
	
	/**
	 * Expression constants are replaced by their values
	 */
	public Object visit(ExpressionVar v) throws PrismLangException
	{
		
		if (v.getName().equals(this.vname)) {
			return this.visit(this.literal);
		}
		// otherwise we leave it unchanged
		return super.visit(v);
	}
	
	/**
	 * If this is an ident which is a constant, this is also replaced
	 */
	public Object visit(ExpressionIdent e) throws PrismLangException
	{
		
		// if the name of the identifier is the same as the variable
		if (e.getName().equals(this.vname)){
			return this.visit(this.literal);
		}
		// Otherwise, leave it unchanged
		return super.visit(e);
	}
	
	/*
	 * We need to replace the occurrence of the variable in every expression, recursively
	 */
	@Override
	public Object visit(ExpressionBinaryOp e) throws PrismLangException
	{
		return new ExpressionBinaryOp(e.getOperator(), this.copy(e.getOperand1()), this.copy(e.getOperand2()));
	}

	@Override
	public Object visit(ExpressionUnaryOp e) throws PrismLangException
	{
		return new ExpressionUnaryOp(e.getOperator(), this.copy(e.getOperand()));
	}

	@Override
	public Object visit(ExpressionFunc e) throws PrismLangException
	{
		ExpressionFunc result = new ExpressionFunc();
		result.setName(e.getName());
		for (int i = 0; i < e.getNumOperands(); i++) {
			result.setOperand(i, this.copy(e.getOperand(i)));
		}
		result.setOldStyle(e.getOldStyle());
		return result;
	}


	@Override
	public Object visit(ExpressionArray e) throws PrismLangException {
		Expression newi = this.copy(e.getI());
		Expression newj = this.copy(e.getJ());
		
		// we create a copy of the curent expression
		DeepCopy copier = new DeepCopy();
		ExpressionArray result = copier.copy(e);
		
		// and we replace the indexes
		result.setI(newi);
		result.setJ(newj);
		return result;
	}

	@Override
	public Object visit(ExpressionMinMax e) throws PrismLangException {
		ExpressionMinMax m = new ExpressionMinMax();
		m.setFunction(e.function());
		m.setLeft(this.copy(e.left()));
		m.setRight(this.copy(e.right()));
		return m;
	}


}
	
	
	
	
	
	
	

