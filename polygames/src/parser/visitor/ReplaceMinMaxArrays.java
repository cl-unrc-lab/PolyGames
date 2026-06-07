package parser.visitor;

import parser.ast.ConstantList;
import parser.ast.Expression;
import parser.ast.ExpressionMinMax;
import parser.ast.ModulesFile;
import parser.type.TypeInt;
import parser.type.TypeDouble;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionArray;



import prism.PrismLangException;


/**
 * Replaces all the occurrences of min max and arrays for the corresponding expressions
 * it is assumed that no constants or variables appear inside max/min and array operators
 * @author pablo
 *
 */

public class ReplaceMinMaxArrays extends ASTTraverseModify{
	private ModulesFile mf;
	
	
	/** The constructor takes as a parameter the ModulesFiles, needed to be able to evaluate the indices
	 * 
	 * @param mf
	 */
	public ReplaceMinMaxArrays(ModulesFile mf)
	{
		this.mf = mf;
	}
	
	/** The visit method for ExpressionMinMax, in this case the two parameters are evaluated and
	 * the expression is replaced for the result
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
	 *  This replaces all the occurrences of array expressions to the corresponding constant
	 *  it is assumed that no identifiers appear in the indexes
	 */
	@Override
	public Object visit(ExpressionArray array) throws PrismLangException
	{	
		Expression left = (Expression) array.getI().accept(this); // we evaluate recursively the indexes
		Expression right = (Expression) array.getJ().accept(this);
		int leftInt = left.evaluateInt(); // if some identifier appears here there will be an exception
		int rightInt = right.evaluateInt();
		
		// we get the number of constant, the constant are enumerated using the formula i*N+j
		int constantNumber = left.evaluateInt() * array.getLineLength() + right.evaluateInt(); 
		// we get the value of the constant in the model
		int constantIndex = this.mf.getConstantList().getConstantIndex(array.getName()+constantNumber);
		if (constantIndex < 0) {
			throw new PrismLangException("Index out of bound in array expression");
		}
		
		
		// and we replace the expression for the corresponding value.
		return this.mf.getConstantList().getConstant(constantIndex);	
		
	}
	
	
}
