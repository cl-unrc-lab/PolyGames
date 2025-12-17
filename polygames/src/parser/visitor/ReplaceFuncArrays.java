package parser.visitor;

import parser.ast.ConstantList;
import parser.ast.Expression;
import parser.ast.ExpressionMinMax;
import parser.ast.ModulesFile;
import parser.type.TypeInt;
import parser.type.TypeDouble;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionArray;
import parser.ast.ExpressionFunc;
import parser.ast.FormulaList;

import java.util.List;

import parser.ast.ASTElement;
import parser.ast.CommandWithArrays;
import parser.ast.ExpressionFunc;
import parser.ast.Command;


import prism.PrismLangException;


/**
 * Replaces all the occurrences of min max and arrays for the corresponding expressions
 * it is assumed that no constants or variables appear inside max/min and array operators
 * @author pablo
 *
 */

public class ReplaceFuncArrays extends ASTTraverseModify{
	private ModulesFile mf;
	private boolean isFormula = false;
	
	
	/** The constructor takes as a parameter the ModulesFiles, needed to be able to evaluate the indices
	 * 
	 * @param mf
	 */
	public ReplaceFuncArrays(ModulesFile mf)
	{
		this.mf = mf;
	}
	
	/** The visit method for ExpressionMinMax, in this case the two parameters are evaluated and
	 * the expression is replaced for the result
	 * the idea is to replace this for a {@code ExpressionFunc} call
	 */
	@Override
	public Object visit(ExpressionMinMax minmax) throws PrismLangException
	{
		ExpressionLiteral result = null; // the result
		// we recursively evaluate the left and right operators
		Expression left = (Expression) minmax.left().accept(this);
		Expression right = (Expression) minmax.right().accept(this);
		
		if (left.getType() == TypeInt.getInstance()) { // if the parameters are ints
			int leftInt = left.evaluateInt();
			int rightInt = right.evaluateInt();
			result = new ExpressionLiteral(TypeInt.getInstance(), Math.max(leftInt, rightInt));
		}
		if (left.getType() == TypeDouble.getInstance()) { // if the parameters are double
			double leftDouble = left.evaluateDouble();
			double rightDouble = right.evaluateDouble();
			result = new ExpressionLiteral(TypeDouble.getInstance(), Math.max(leftDouble, rightDouble));
		}
		return result;
	}
	
	/**
	 * A method to evaluate functions, it is assumed that all variables and constants have been replaced
	 */
	@Override
	public Object visit(ExpressionFunc func) throws PrismLangException
	{
		ExpressionFunc newExp = new ExpressionFunc(); // the result
		newExp.setName(func.getName());
		ExpressionLiteral result = null;
		// we recursively evaluate the left and right operators
		// we visit all the parameters and recursively evaluate all of them
		for (int i = 0; i < func.getNumOperands(); i++) {
			newExp.addOperand((Expression) func.getOperand(i).accept(this));
		}
		
		try {
			if (func.getType() == TypeInt.getInstance()) { // if int 
				 result = new ExpressionLiteral(TypeInt.getInstance(), newExp.evaluate(mf.getEvaluateContext()));
			}
			else { // otherwise is a double //if (func.getType() == TypeDouble.getInstance()) { 
				result = new ExpressionLiteral(TypeDouble.getInstance(), newExp.evaluate(mf.getEvaluateContext()));
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//}
		//else {
		//	throw new PrismLangException("Type error in equation system.");
		//}
		return result;
	}
	
	
	public Object visit(CommandWithArrays c) throws PrismLangException{
		Command new_command = (Command) super.visit(c);
		Command result = new Command();
		result.setParent(c.getParent());
		result.setGuard(new_command.getGuard());
		result.setUpdates(new_command.getUpdates());
		result.setSynch(new_command.getSynch());
		//command
		return  result;
	}
	
	public Object visit(Command c) throws PrismLangException{
		return super.visit(c);
	}
	
	public Object visit(FormulaList fl) throws PrismLangException{
		FormulaList result = new FormulaList();
		ASTElementSearcherVisitor searcherArrays = new ASTElementSearcherVisitor(ExpressionArray.class);
		ASTElementSearcherVisitor searcherFuncs = new ASTElementSearcherVisitor(ExpressionFunc.class);
		for (int i = 0; i < fl.size(); i++) {
			List<ASTElement> elements = (List<ASTElement>) fl.getFormula(i).accept(searcherArrays);
			elements.addAll((List<ASTElement>) fl.getFormula(i).accept(searcherFuncs));
			if (elements.size() > 0) {
				result.addFormula(fl.getFormulaNameIdent(i), (Expression) fl.getFormula(i).accept(this));
			}
			else {
				result.addFormula(fl.getFormulaNameIdent(i), (Expression) fl.getFormula(i).accept(this));
			}
		}
		
		return result;
	}
	
	/**
	 *  This replaces all the occurrences of array expressions to the corresponding constant
	 *  it is assumed that no identifiers appear in the indexes
	 */
	@Override
	public Object visit(ExpressionArray array) throws PrismLangException
	{	
		Expression left = (Expression) array.getI().accept(this); // we evaluate recursively the indexes
		Expression right = (Expression) array.getJ().accept(this);
		try {
			int leftInt = left.evaluateInt(); // if some identifier appears here there will be an exception
			int rightInt = right.evaluateInt();
		}
		catch(Exception e){
			
			e.printStackTrace();
		}
		
		// we get the number of constant, the constant are enumerated using the formula i*N+j
		int constantNumber = left.evaluateInt() * array.getLineLength() + right.evaluateInt(); 
		// we get the value of the constant in the model
		//System.out.println(this.mf.getConstantList());
		//System.out.println(constantNumber);
		int constantIndex = this.mf.getConstantList().getConstantIndex(array.getName()+constantNumber);
		if (constantIndex < 0) {
			throw new PrismLangException("Index out of bound in array expression:"+array);
		}
		
		
		// and we replace the expression for the corresponding value.
		return this.mf.getConstantList().getConstant(constantIndex);	
		
	}
	
	
	
	
}
