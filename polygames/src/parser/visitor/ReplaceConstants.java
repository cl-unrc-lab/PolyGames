package parser.visitor;


import java.util.List;

import parser.ast.*;
import parser.type.Type;
import prism.PrismLangException;



/**
 * 
 * @author pablo
 * REplaces all the ExpressionConstants by their corresponding definition
 *
 */
public class ReplaceConstants extends ASTTraverseModify{
	
	private ConstantList constantList;
	
	
	public ReplaceConstants(ConstantList constantList)
	{
		this.constantList = constantList;
	}
	
	
	/**
	 * Expression constants are replaced byt their values
	 */
	public Object visit(ExpressionConstant c) throws PrismLangException
	{
		// See if identifier corresponds to a constant
		int i = constantList.getConstantIndex(c.getName());
		if (i != -1) {
			return constantList.getConstant(i);
			
		}
		// Otherwise, leave it unchanged
		return c;
	}
	
	/**
	 * If this is an ident which is a constant, this is also replaced
	 */
	public Object visit(ExpressionIdent e) throws PrismLangException
	{
		System.out.println("expression:"+e);
		// See if identifier corresponds to a constant
		int i = constantList.getConstantIndex(e.getName());
		if (i != -1) {
			// If so, replace it with an ExpressionConstant object
			Expression expr = constantList.getConstant(i);
			expr.setPosition(e);
			return expr;
		}
		// Otherwise, leave it unchanged
		return e;
	}
}

