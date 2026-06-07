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
	 * Expression constants are replaced by their values.
	 * If the constant has no value yet (e.g. it will be supplied via -const on the
	 * command line), leave it unchanged so downstream visitors don't receive null.
	 */
	public Object visit(ExpressionConstant c) throws PrismLangException
	{
		int i = constantList.getConstantIndex(c.getName());
		if (i != -1) {
			Expression val = constantList.getConstant(i);
			if (val != null) return val;
		}
		// Undefined constant — leave unchanged for PRISM to resolve at model-check time
		return c;
	}

	/**
	 * If this is an ident which is a constant, replace it with its value.
	 * If the constant has no value yet, leave it unchanged.
	 */
	public Object visit(ExpressionIdent e) throws PrismLangException
	{
		int i = constantList.getConstantIndex(e.getName());
		if (i != -1) {
			Expression val = constantList.getConstant(i);
			if (val != null) {
				val.setPosition(e);
				return val;
			}
		}
		// Undefined constant or not a constant — leave unchanged
		return e;
	}
}

