package parser.visitor;

import parser.ast.Expression;
import parser.ast.ExpressionIdent;
import parser.ast.FormulaList;
import parser.ast.ModulesFile;
import prism.PrismLangException;
import parser.ast.ExpressionITE;
import parser.ast.ExpressionFormula;
import parser.ast.Command;

/**
 * This class evaluates an ITE expression (conditional expression) and replace the expression by the obtained value
 * it is assumed that all variables, constants have been replaced by their values. 
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
	
	
	public Object visit(FormulaList e) {
		return e; // for formula list we do not replace anything
	}
	

	public Object visit(Command c) {
		return c; // for command we do not replace anything
	}
	/**
	 * If this is an ITE Expression, we replace it  for its definition
	 */
	public Object visit(ExpressionITE e) throws PrismLangException
	{
		// we recursively apply the visitor to operand2 and operand3 
		//Expression operand1 = (Expression) e.getOperand1().accept(this);
		//Expression operand2 = (Expression) e.getOperand2().accept(this);
		//Expression operand3 = null; // the else could be null
		//if (e.getOperand3() != null) {
		//	operand3 = e.getOperand3();
		//}
		Boolean guard;
		try {
			//guard = operand1.evaluateBoolean(mf.getEvaluateContext());
			guard = e.getOperand1().evaluateBoolean(mf.getEvaluateContext());
		}
		catch (Exception exception) {
			return e;
		}
		if (guard) {
			Expression operand2;
			operand2 = (Expression) e.getOperand2().accept(this);
			return operand2;
		}
		else {
			if (e.getOperand3() == null) {
				throw new PrismLangException("Formula of line "+e.getBeginLine()+" is partially defined.");
			}
			return (Expression) e.getOperand3().accept(this);
		}
	}
}
