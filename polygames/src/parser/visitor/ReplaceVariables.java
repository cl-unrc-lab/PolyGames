package parser.visitor;

import java.util.ArrayList;
import java.util.HashMap;

import parser.ast.Command;
import parser.ast.Declaration;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionLiteral;
import parser.ast.Module;
import parser.ast.DeclarationInt;
import parser.ast.DeclarationBool;
import parser.ast.Update;
import prism.PrismLangException;
import parser.ast.ModulesFile;
import parser.ast.UncertainUpdates;
import parser.ast.Updates;
import parser.type.TypeInt;
import parser.type.TypeBool;
import parser.ast.UpdateElement;



/**
 * This class extends DeepCopy and produces a copy of the given object where, in the commands, the variables 
 * are replaced by its possible values. For instance:
 * 
 * v : [0..n] 
 * 
 * [a] g -> c
 * 
 * will be replaced by 
 * 
 * [a] g[v:=0] -> c[v:=0]
 * ...
 * [a] g[v:=n] -> c[v:=n]
 * 
 * it uses the class ReplaceVariable to replace the occurrence of each variables in the expressions
 * 
 * @author pablo
 *
 */
public class ReplaceVariables extends DeepCopy {
	private ModulesFile mf; // the modules file, needed  for obtaining all the values of the variables
	private Module m; // the module, needed for obtaining the local variables
	private String currentVar; // these are used to signal the current variable being used
	private ExpressionLiteral currentVal;

	public ReplaceVariables() {
		this.mf = null;
		this.m = null;
	}

	/**
	 * this only sets the current modules file and call the super implmentation of the visitor
	 */
	public Object visit(ModulesFile m) {
		this.mf = m;
		try {
			return super.visit(mf);
		} catch (PrismLangException e) {
			System.out.println("Error while processing the module files");
		}
		return m;
	}

	/**
	 * This copies a module generating several commands for every command, each of this command is obtained
	 * by replacing a variable with one of its possible values.
	 */
	@Override
	public Object visit(Module e) throws PrismLangException {
		Module result = new Module(e.getName()); // we create a new module with the same name
		result.setInvariant(this.copy(e.getInvariant())); // we set the invariant
		result.setNameASTElement(this.copy(e.getNameASTElement())); 
		
		// we copy all the declarations
		for (Declaration declaration : this.copyAll(e.getDeclarations())) {
			result.addDeclaration(declaration);
		}

		this.m = e; // we set the current module to being visited
		
		// we visit all the commands
		for (Command command : e.getCommands()) {
			// for each command 
			for (Command cc : this.visit(command)) {
				result.addCommand(cc);
			}
		}

		return result;
	}

	/**
	 * This method provides the new functionality for deepcopy, it creates several copies of the command
	 * one for each value of the variable.
	 */
	@Override
	public ArrayList<Command> visit(Command e) throws PrismLangException {

		// the declarations are stored since we need to iterate over them
		ArrayList<Declaration> decls = new ArrayList<Declaration>(m.getDeclarations());

		// the following are the global vars, we need to replace the global and local vars
		for (int i = 0; i < mf.getNumGlobals(); i++) {
			decls.add(mf.getGlobal(i));
		}
		
		// a list to store the resulting commands
		ArrayList<Command> resultingCommands= new ArrayList<Command>();
		// we add the actual command to the list (this will be updated later)
		resultingCommands.add(e);
		// for each var declaration  we need to create the corresponding collection of updates
		for (Declaration decl : decls) {
			this.currentVar = decl.getName(); // the current var
			Expression new_guard = null; // var to store the new guards
			Updates new_updates = null; // var to store the curren updates

			// the temporary new commands are stored in the following list
			ArrayList<Command> tempCommands = new ArrayList<Command>();
			for (Command c : resultingCommands) {
				// if the var is integer
				if (decl.getDeclType() instanceof DeclarationInt) {
					DeclarationInt declInt = (DeclarationInt) decl.getDeclType();
					// the lowest value of the var
					Integer low = (Integer) declInt.getLow().evaluate(this.mf.getEvaluateContext());
					// the higher value
					Integer high = (Integer) declInt.getHigh().evaluate(this.mf.getEvaluateContext());
					
					// for any value in the interval we replace the variable for its value
					for (int i = low; i <= high; i++) {
						this.currentVal = new ExpressionLiteral(TypeInt.getInstance(), i);
						
						// for a variable x, we generate the equality x=v for the given value v,
						// this will be part of the guard
						Expression actualValue = new ExpressionBinaryOp(ExpressionBinaryOp.EQ,
								new ExpressionIdent(this.currentVar), new ExpressionLiteral(TypeInt.getInstance(), i));
						
						// we replace the var in the guard
						ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
						new_guard = (Expression) c.getGuard().accept(replacer);
						new_guard = new ExpressionBinaryOp(ExpressionBinaryOp.AND, actualValue, new_guard);
	
						// we visit the updates
						new_updates = this.copy(c.getUpdates());
						// and we add the command
						Command command = new Command();
						command.setSynch(c.getSynch());
						command.setParent(c.getParent());
						command.setGuard(new_guard);
						command.setUpdates(new_updates);
						tempCommands.add(command);
	
					}
				}
				// if boolean, the procedure is similar but we iterate over {false,true}
				if (decl.getDeclType() instanceof DeclarationBool) {
					DeclarationBool declBool = (DeclarationBool) decl.getDeclType();
	
					ArrayList<Boolean> bs = new ArrayList<Boolean>();
					bs.add(true);
					bs.add(false);
					for (boolean b : bs) {
						this.currentVal = new ExpressionLiteral(TypeBool.getInstance(), b);
						Expression actualValue = new ExpressionBinaryOp(ExpressionBinaryOp.EQ,
								new ExpressionIdent(this.currentVar), new ExpressionLiteral(TypeInt.getInstance(), b));
						new_guard = (Expression) c.getGuard().accept(this);
						new_guard = new ExpressionBinaryOp(ExpressionBinaryOp.AND, actualValue, new_guard);
	
						// we visit the updates
						new_updates = (Updates) c.getUpdates().accept(this);	
						
						// and we add the command
						Command command = new Command();
						new_updates.setParent(command);
						command.setSynch(c.getSynch());
						command.setParent(c.getParent());
						command.setGuard(new_guard);
						command.setUpdates(new_updates);
						tempCommands.add(command);
					}//endfor
				}//endif
			}//endfor
			resultingCommands = tempCommands; // we update the resulting commands
		}// endfor
		return resultingCommands;
		
	}


	/*
	 * Visit method for UncertainUpdates, in this case we also need to replace the indices in the 
	 * equations. We have to override the method of DeepCopy otherwise it calls to DeepCopy and the
	 * variables are no replaced.
	 */
	//@Override
	public UncertainUpdates visit(UncertainUpdates e) throws PrismLangException {
		
		// a replacer for replacing the variables
		ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
		// ac opier for copying the part that not need to be replaced
		DeepCopy copier = new DeepCopy();
		
		// the result
		UncertainUpdates result = new UncertainUpdates();
		
		// we copy the uncertains and the constants
		ArrayList<Expression> newuncertains = (ArrayList<Expression>) copier.copyAll(e.getUncertains());
		ArrayList<Expression> newconstants  = (ArrayList<Expression>) copier.copyAll(e.constants());
		
		
		// we replace the variables in the constants and the uncertains
		for (Expression c : newconstants) {
			Expression ecopy = replacer.copy(c);
			result.constants().add(ecopy);
		}
		
		for (Expression u : newuncertains) {
			Expression ecopy = copier.copy(u);
			result.getUncertains().add(ecopy);
		}
		
		// we replace the coefficients
		for (String key : e.getCoefficients().keySet()) { // for all uncertain names
			HashMap<Integer, Expression> uncertain_coeffs = e.getCoefficients().get(key);
			for (Integer i : uncertain_coeffs.keySet()) {
				result.setCoefficient(key, i, (Expression) replacer.copy(uncertain_coeffs.get(i)));
			}
				
		}
		result.getRelations().addAll(e.getRelations()); // we do not neet to replace variables in the relations
		for (Update u : e.getUpdates()) {
			Update newUpdate = (Update) this.copy(u);
			newUpdate.setParent(result);
			result.getUpdates().add(newUpdate);
		}
		// we also replace variables in the probabilities, they are expressions
		result.getProbabilities().addAll(copier.copyAll(e.getProbabilities()));
		return result;
		
	}
	
	/*
	 * Method for dealing with the updates, we cannot rely in DeepCopy original method
	 */
	public Updates visit(Updates us) throws PrismLangException{
		 Updates result = new Updates();
		 for (Update u: us.getUpdates()){
			 Update newUpdate = this.copy(u);
			 newUpdate.setParent(result);
			 result.getUpdates().add(newUpdate); // we copy each update
		 }
		 for (Expression p:us.getProbabilities()){
			 result.getProbabilities().add(this.copy(p)); // we copy the probabilities
		 }
		 // we need to set the parent of this to the parent of the original, this is done one level up
		 return result;
		
	}
	
	/*
	 * Method for dealing with the common updates, we process every update element 
	 */
	public Update visit(Update u) throws PrismLangException {
		Update result = new Update();
		for (UpdateElement ue : u.getElements()){
			UpdateElement newUE = (UpdateElement) ue.accept(this);
			result.addElement(newUE);
		}
		return result;
	}
	
	/*
	 * The update elements are processed, we chanfe the variables in the expression
	 */
	public UpdateElement visit(UpdateElement ue) throws PrismLangException {
		
		DeepCopy copier = new DeepCopy();
		UpdateElement new_ue = copier.copy(ue); // we copy the element
		ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
		new_ue.setExpression(replacer.copy(ue.getExpression())); // we replace only the expression part
		new_ue.setVarIdent(copier.copy(ue.getVarIdent()));
		new_ue.setVarIndex(ue.getVarIndex());
		return new_ue;
		
		
	}
	

	
}
