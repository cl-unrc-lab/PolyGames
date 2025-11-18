package parser.visitor;

import parser.ast.Expression;
import parser.ast.ExpressionIdent;
import parser.ast.FormulaList;
import parser.ast.ModulesFile;
import prism.PrismLangException;
import parser.ast.ExpressionITE;

/**
 * This class evaluates an ITE expression (conditional expression) and replace the expresion by the obtained value
 * it is assumed that all variables, constants have been replace by their values. 
 * @author pablo
 *
 */

public class ReplaceExpressionITE extends ASTTraverseModify {

	private ModulesFile mf;
	/**
	 * Basic constructor
	 * @param mf the moduleFiles, needed to evaluate the expressions
	 */
	public ReplaceExpressionITE(ModulesFile mf)
	{
		this.mf = mf;
	}
	
	/**
	 * If this is an ITE Expression, we replace it  for its definition
	 */
	public Object visit(ExpressionITE e) throws PrismLangException
	{
		// we recursively apply the visitor to operand2 and operand3 
		Expression operand1 = (Expression) e.getOperand1().accept(this);
		Expression operand2 = (Expression) e.getOperand1().accept(this);
		Expression operand3 = null; // the else could be null
		if (e.getOperand3() != null) {
			operand3 = e.getOperand3();
		}
		
		Boolean guard = operand1.evaluateBoolean();
		if (guard) {
			return operand2;
		}
		else {
			if (operand3 == null) {
				throw new PrismLangException("Formula of line "+e.getBeginLine()+" is partially defined.");
			}
			return operand3;
		}
	}
}
