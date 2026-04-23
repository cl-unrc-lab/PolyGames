package parser.visitor;


import java.util.List;
import java.util.HashMap;

import parser.ast.*;
import parser.type.Type;
import parser.type.TypeInt;
import parser.type.TypeDouble;
import parser.type.TypeBool;
import prism.PrismLangException;



/**
 * 
 * @author pablo
 * REplaces all the ExpressionConstants by their corresponding definition
 *
 */
public class ReplaceConstants extends ASTTraverseModify{
	
	private ConstantList constantList;
	private HashMap<String, Expression> undefCons;
	
	
	
	public ReplaceConstants(ConstantList constantList, String constSwitch) throws PrismLangException
	{
		this.constantList = constantList;
		this.undefCons = new HashMap<String, Expression>();
		String[] pairs = constSwitch.split(",");

        for (String pair : pairs) {
            // Split each pair by the equals sign
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                // Determine the type and store in the correct map
                if (isInt(value)) {
                    undefCons.put(key, new ExpressionLiteral(TypeInt.getInstance(), Integer.parseInt(value)));
                } else if (isDouble(value)) {
                	undefCons.put(key, new ExpressionLiteral(TypeDouble.getInstance(), Double.parseDouble(value)));
                }
                 else if (isBool(value)) {

                	undefCons.put(key, new ExpressionLiteral(TypeBool.getInstance(), Boolean.parseBoolean(value)));
                } else {
                    // Handle other types or log an error if necessary
                	throw new PrismLangException("Undefined Constant.");
                }
                
            }
        }
		
				
	}
	
	
	/**
	 * Expression constants are replaced by their values
	 */
	public Object visit(ExpressionConstant c) throws PrismLangException
	{
		// See if identifier corresponds to a constant
		int i = constantList.getConstantIndex(c.getName());
		if (i != -1) {
			if (constantList.getConstant(i) != null)
				return constantList.getConstant(i);
			else 
				return undefCons.get(c.getName());
			
		}
		// Otherwise, leave it unchanged
		return c;
	}
	
	/**
	 * If this is an ident which is a constant, this is also replaced
	 */
	public Object visit(ExpressionIdent e) throws PrismLangException
	{
		// See if identifier corresponds to a constant
		int i = constantList.getConstantIndex(e.getName());
		if (i != -1) {
			if (constantList.getConstant(i) != null) {
			// If so, replace it with an ExpressionConstant object
				Expression expr = constantList.getConstant(i);
				expr.setPosition(e);
				return expr;
			}
			else {
				return undefCons.get(e.getName());
			}
		}
		// Otherwise, leave it unchanged
		return e;
	}
	
	

    // Helper methods to check the data type of a string value
    private static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBool(String str) {
        // Check for case-insensitive "true" or "false"
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }
}

