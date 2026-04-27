package parser.visitor;


import parser.ast.ExpressionBinaryOp;
import prism.PrismLangException;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionVar;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionUnaryOp;
import parser.type.*;


/**
 * A Simple class to check if a expression has an obvious inconsistency, used to simplify guards
 * @author pablo
 *
 */

public class CheckInconsistency extends ASTTraverse {
	
	public Object visit(ExpressionLiteral exp) throws PrismLangException{
		return exp.getValue(); // it returns the value of the literal
	}
	
	public Object visit(ExpressionVar exp) throws PrismLangException{
		return null; // it returns the value of the literal
	}
	
	public Object visit(ExpressionIdent exp) throws PrismLangException{
		return null; // it returns the value of the literal
	}
	
	public Object visit(ExpressionUnaryOp exp) throws PrismLangException{
		// false by default
		if (exp.getOperator() == ExpressionUnaryOp.PARENTH) {
			boolean result = (Boolean) exp.getOperand().accept(this);
			return result;
		}
		if (exp.getOperator() == ExpressionUnaryOp.NOT) {
			if (exp.getOperand() instanceof ExpressionLiteral) {
				ExpressionLiteral op = (ExpressionLiteral) exp.getOperand();
				return (Boolean) op.getValue() == true;
			}
		}
		return false;
	}
	
	
	public Object visit(ExpressionBinaryOp exp) throws PrismLangException{
		// if it is a boolean operator
		if (exp.getOperator() == ExpressionBinaryOp.AND || exp.getOperator() == ExpressionBinaryOp.OR || exp.getOperator() == ExpressionBinaryOp.IMPLIES || exp.getOperator() == ExpressionBinaryOp.IFF) {
			
			Boolean resultop1 = false;
			Boolean resultop2 = false;
			try {
				resultop1 = (Boolean) exp.getOperand1().accept(this);
			}
			catch(Exception e) {
				resultop1 = null;
				
			}
			try {
				resultop2 = (Boolean) exp.getOperand2().accept(this);
			}
			catch(Exception e) {
				resultop2 = null;
			}
			
			switch (exp.getOperator()) {
				case ExpressionBinaryOp.AND :
					return ((resultop1 != null && resultop1 == true) ||  (resultop2 != null && resultop2 == true));
				case ExpressionBinaryOp.OR :
					return ((resultop1 != null && resultop1 == true) &&  (resultop2 != null && resultop2 == true));
				case ExpressionBinaryOp.IMPLIES:
					return ((resultop1 != null && resultop1 == false) && (resultop2 != null && resultop2 == true));
				case ExpressionBinaryOp.IFF:
					return (resultop1 != null &&  resultop2 != null  && ((resultop1 == false && resultop2 == true) || (resultop1 == true && resultop2 == false)));
				default :
				 
			}
			
		}// end case boolean operator
		
		if (exp.getOperator() == ExpressionBinaryOp.EQ || exp.getOperator() == ExpressionBinaryOp.GE || exp.getOperator() == ExpressionBinaryOp.GT || exp.getOperator() == ExpressionBinaryOp.LE || exp.getOperator() == ExpressionBinaryOp.LT) {
			
			Integer resultop1Int = 0;
			Integer resultop2Int = 0;
			try {
				resultop1Int = (Integer) exp.getOperand1().accept(this);
			}
			catch(Exception exception) {
				resultop1Int = null;
				
			}
			try {
				resultop2Int = (Integer) exp.getOperand2().accept(this);
			}
			catch(Exception exception) {
				resultop2Int = null;
			}
			
			//System.out.println("Note:"+exp);
			//System.out.println("Note:"+resultop1Int);
			//System.out.println("Note:"+resultop2Int);
			switch (exp.getOperator()) {
				case ExpressionBinaryOp.EQ :
					return resultop1Int != null && resultop2Int != null && (resultop1Int != resultop2Int); // if the ops are different the guard is false and we return true
				case ExpressionBinaryOp.GE :
					return resultop1Int != null && resultop2Int != null && (resultop1Int < resultop2Int);
				case ExpressionBinaryOp.GT:
					return resultop1Int != null && resultop2Int != null && (resultop1Int <= resultop2Int);
				case ExpressionBinaryOp.LE:
					return resultop1Int != null && resultop2Int != null && (resultop1Int > resultop2Int);
				case ExpressionBinaryOp.LT:
					return resultop1Int != null && resultop2Int != null && (resultop1Int >= resultop2Int);
				default :
					return false;
			}
		
		} // end case relational operators		
		return false;
		
	} // end BinaryExpressionOp

}
