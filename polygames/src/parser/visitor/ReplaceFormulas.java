package parser.visitor;

import java.util.List;

import parser.ast.*;
import parser.type.Type;
import prism.PrismLangException;



/**
 * 
 * @author pablo
 * Replaces formulas for their definitions
 *
 */
public class ReplaceFormulas extends ASTTraverseModify{
	
	private ModulesFile mf;
	
	
	public ReplaceFormulas(ModulesFile mf)
	{
		this.mf = mf;
	}
	
	
	/**
	 * If this is an ident which is a formula, we replace it  for its definition
	 */
	public Object visit(ExpressionIdent e) throws PrismLangException
	{
		// See if identifier corresponds to a constant
		Expression result = e;
		
		FormulaList flist = mf.getFormulaList();
		
		// we get the index of the formula, -1 if this does not exist
		int formulaIndex = flist.getFormulaIndex(e.getName());
		if (formulaIndex > -1) { // if defined
			result = new ExpressionUnaryOp(ExpressionUnaryOp.PARENTH,flist.getFormula(formulaIndex));
		}
		return result;
	}
	
}
