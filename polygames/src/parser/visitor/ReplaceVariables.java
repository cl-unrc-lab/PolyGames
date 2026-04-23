package parser.visitor;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.logging.log4j.core.util.SystemNanoClock;

import java.util.HashMap;
import java.util.List;
import parser.ast.ASTElement;
import parser.ast.Command;
import parser.ast.Declaration;
import parser.ast.Expression;
import parser.ast.ExpressionBinaryOp;
import parser.ast.ExpressionIdent;
import parser.ast.ExpressionLiteral;
import parser.ast.ExpressionArray;
import parser.ast.Module;
import parser.ast.DeclarationInt;
import parser.ast.DeclarationBool;
import parser.ast.Update;
import parser.ast.ExpressionVar;
import prism.PrismLangException;
import parser.ast.ModulesFile;
import parser.ast.UncertainUpdates;
import parser.ast.Updates;
import parser.type.TypeInt;
import parser.type.TypeBool;
import parser.ast.UpdateElement;
import parser.ast.FormulaList;
import parser.ast.ExpressionITE;
import parser.ast.ExpressionUnaryOp;
import parser.ast.ExpressionFunc;
import parser.visitor.ReplaceExpressionITE;


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
 * This proceeds similarly for formulas, but only in those cases that the formulas contain arrays
 * 
 * @author pablo
 *
 */
public class ReplaceVariables extends ASTTraverseModify {
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
		if (e.getInvariant() != null)
			result.setInvariant((Expression) e.getInvariant().accept(this)); // we set the invariant
		result.setNameASTElement((ExpressionIdent) e.getNameASTElement().accept(this)); 
		
		// we copy all the declarations
		//for (Declaration declaration : this.copyAll(e.getDeclarations())) {
		//	result.addDeclaration(declaration);
		//}
		for (Declaration declaration : e.getDeclarations()) {
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
		//if (!(e.getUpdates() instanceof UncertainUpdates)){
		//	ArrayList<Command> resultingCommands= new ArrayList<Command>();
		//	resultingCommands.add(e);
		//	return resultingCommands;
		//}
		// if there is no arrays we just return the same command
		
		e.setSynch(e.getSynch());
		e.setSynchIndices(e.getSynchs());
		ArrayList<Command> resultingCommands= new ArrayList<Command>();
		
		
		ASTElementSearcherVisitor arraySearcher = new ASTElementSearcherVisitor(ExpressionArray.class);
		List<Expression> arraysInCommand = (List<Expression>) arraySearcher.visit(e);
		if (arraysInCommand.size() == 0 && !(e.getUpdates() instanceof UncertainUpdates)) {

			resultingCommands.add(e);
			return resultingCommands;
		}
		
		
		ASTElementSearcherVisitor searcher = new ASTElementSearcherVisitor(ExpressionIdent.class);
		List<ExpressionIdent> identsInCommand = (List<ExpressionIdent>) searcher.visit(e);
		ArrayList<String> identNamesInCommand = new ArrayList<String>();
		for (ExpressionIdent ident : identsInCommand) {
			identNamesInCommand.add(ident.getName());
		}
		
			
		// the declarations are stored since we need to iterate over them
		ArrayList<Declaration> decls = new ArrayList<Declaration>(m.getDeclarations());

		// the following are the global vars, we need to replace the global and local vars
		for (int i = 0; i < mf.getNumGlobals(); i++) {
			decls.add(mf.getGlobal(i));
		}
		
		// a list to store the resulting commands
		//ArrayList<Command> resultingCommands= new ArrayList<Command>();
		// we add the actual command to the list (this will be updated later)
		resultingCommands.add(e);
		// for each var declaration  we need to create the corresponding collection of updates
		for (Declaration decl : decls) {
			this.currentVar = decl.getName(); // the current var
			//System.out.println(identNamesInCommand);
			Expression new_guard = null; // var to store the new guards
			Updates new_updates = null; // var to store the current updates
			// the temporary new commands are stored in the following list
			ArrayList<Command> tempCommands = new ArrayList<Command>();
			int k = 0;
			for (Command c : resultingCommands) {
				//System.out.println("*******************Command:"+c);
				// if the var is integer
				//System.out.println("******** Command:"+c);
				
				searcher = new ASTElementSearcherVisitor(ExpressionIdent.class);
				List<ExpressionIdent> identsInUpdates = (List<ExpressionIdent>) c.getUpdates().accept(searcher);
				ArrayList<String> identNamesInUpdates = new ArrayList<String>();
				for (ExpressionIdent ident : identsInUpdates) {
					identNamesInUpdates.add(ident.getName());
				}
				
				
				
				if (decl.getDeclType() instanceof DeclarationInt) {
					DeclarationInt declInt = (DeclarationInt) decl.getDeclType();
					
					if (!identNamesInUpdates.contains(this.currentVar)) { // if the var is not the updates we continue adn the comman is unchanged
						tempCommands.add(c);
						continue;
					}
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
						
						
						CheckInconsistency checker = new CheckInconsistency(); 
						// if the guard is inconsistent then pass
						if ((Boolean) new_guard.accept(checker)) {
							continue;
						}
						
						// we visit the updates
						new_updates = (Updates) c.getUpdates().accept(this);
						ReplaceExpressionITE ITEevaluator = new ReplaceExpressionITE(this.mf);
						new_updates = (Updates) new_updates.accept(ITEevaluator);
						// and we add the command
						Command command = new Command();
						command.setSynch(e.getSynch());
						command.setSynchIndices(e.getSynchs());
						command.setParent(c.getParent());
						command.setGuard(new_guard);
						command.setUpdates(new_updates);
						tempCommands.add(command);
							
					}
				}
				//if (decl.getDeclType() instanceof DeclarationBool){
				//	tempCommands = resultingCommands;
				//}
					
				
				// if boolean we just add it
				if (decl.getDeclType() instanceof DeclarationBool)
					tempCommands.add(c);
				/*
				// if boolean, the procedure is similar but we iterate over {false,true}
				if (decl.getDeclType() instanceof DeclarationBool) {
					DeclarationBool declBool = (DeclarationBool) decl.getDeclType();
	
					ArrayList<Boolean> bs = new ArrayList<Boolean>();
					bs.add(true);
					bs.add(false);
					for (boolean b : bs) {
						this.currentVal = new ExpressionLiteral(TypeBool.getInstance(), b);
						Expression actualValue = new ExpressionBinaryOp(ExpressionBinaryOp.EQ,
								new ExpressionIdent(this.currentVar), new ExpressionLiteral(TypeBool.getInstance(), b));
						
						ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
						new_guard = (Expression) c.getGuard().accept(replacer);
						new_guard = new ExpressionBinaryOp(ExpressionBinaryOp.AND, actualValue, new_guard);

						CheckInconsistency checker = new CheckInconsistency(); 
						if ((Boolean) new_guard.accept(checker)) {
							//	System.out.println(new_guard);
								continue;
							}
						//System.out.println(new_guard);
						
						// we visit the updates
						new_updates = (Updates) c.getUpdates().accept(this);
						
						// and we add the command
						Command command = new Command();
						//new_updates.setParent(c.getParent());
						command.setSynch(c.getSynch());
						command.setParent(c.getParent());
						command.setGuard(new_guard);
						command.setUpdates(new_updates);
						if (!tempCommands.contains(command))
							tempCommands.add(command);
					}//endfor
				}//endif
				*/
				
				
			}//endfor
			resultingCommands = tempCommands;
			
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
		//ArrayList<Expression> newuncertains = (ArrayList<Expression>) copier.copyAll(e.getUncertains());
		//ArrayList<Expression> newconstants  = (ArrayList<Expression>) copier.copyAll(e.constants());
		ArrayList<Expression> newuncertains = e.getUncertains();
		ArrayList<Expression> newconstants  = (ArrayList<Expression>) e.constants();
				
		// we replace the variables in the constants and the uncertains
		for (Expression c : newconstants) {
			Expression ecopy = (Expression) c.accept(replacer);
			result.constants().add(ecopy);
		}
				
		for (Expression u : newuncertains) {
			//Expression ecopy = copier.copy(u);
			//result.getUncertains().add(ecopy);
			result.getUncertains().add(u);
		}
				
		// we replace the coefficients
		for (String key : e.getCoefficients().keySet()) { // for all uncertain names
			HashMap<Integer, Expression> uncertain_coeffs = e.getCoefficients().get(key);
			for (Integer i : uncertain_coeffs.keySet()) {
				result.setCoefficient(key, i, (Expression) uncertain_coeffs.get(i).accept(replacer));
			}
						
		}
		result.getRelations().addAll(e.getRelations()); // we do not neet to replace variables in the relations
		for (Update u : e.getUpdates()) {
			Update newUpdate = (Update) u.accept(this);
			newUpdate.setParent(result);
			result.getUpdates().add(newUpdate);
		}
				// we also replace variables in the probabilities, they are expressions
		//result.getProbabilities().addAll(copier.copyAll(e.getProbabilities()));
		result.getProbabilities().addAll(e.getProbabilities());
		return result;
		
		
		/*
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
		*/
	}
	
	/*
	 * Method for dealing with the updates, we cannot rely in DeepCopy original method
	 */
	public Updates visit(Updates us) throws PrismLangException{
		 
		 Updates result = new Updates();
		 for (Update u: us.getUpdates()){
			 Update newUpdate = (Update) u.accept(this);
			 newUpdate.setParent(result);
			 result.getUpdates().add(newUpdate); // we copy each update
		 }
		 
		 for (Expression p:us.getProbabilities()){
			 if (p != null) {
				 ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
				 Expression new_prob = (Expression) p.accept(replacer);
				 result.getProbabilities().add(new_prob); // we copy the probabilities
			 }
			 else {
			 //result.getProbabilities().add((Expression) this.copy(p).accept(replacer)); // we copy the probabilities
			 //System.out.println(p.getClass());
				 result.getProbabilities().add(null); // we copy the probabilities
			 }
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
	 * The update elements are processed, we change the variables in the expression
	 */
	public UpdateElement visit(UpdateElement ue) throws PrismLangException {
		
		DeepCopy copier = new DeepCopy();
		UpdateElement new_ue = copier.copy(ue); // we copy the element
		ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
		new_ue.setExpression((Expression) ue.getExpression().accept(replacer)); // we replace only the expression part
		new_ue.setVarIdent(copier.copy(ue.getVarIdent()));
		new_ue.setVarIndex(ue.getVarIndex());
		return new_ue;
	}
	
	
	
	/**
	 * 
	 */
	
	public FormulaList visit(FormulaList fl) throws PrismLangException {
		ArrayList<Declaration> decls = this.mf.getGlobalVars(); // the global vars
		for (Declaration decl : decls) {
			this.currentVar = decl.getName(); // the current var
			for (int i=0; i < fl.size(); i++) { // for all formulas
				Expression e = fl.getFormula(i);
				ASTElementSearcherVisitor identSearcher = new ASTElementSearcherVisitor(ExpressionIdent.class);	
				List<ExpressionIdent> identsInExp = (List<ExpressionIdent>) e.accept(identSearcher);
				ArrayList<String> varsInExp = new ArrayList<String>();
				for (ExpressionIdent ident : identsInExp) {
					varsInExp.add(ident.getName());
				}
				
				ASTElementSearcherVisitor searcherArrays = new ASTElementSearcherVisitor(ExpressionArray.class);
				ASTElementSearcherVisitor searcherFuncs = new ASTElementSearcherVisitor(ExpressionFunc.class);
				
				List<ASTElement> elements = (List<ASTElement>) e.accept(searcherArrays);
				elements.addAll((List<ASTElement>) e.accept(searcherFuncs));
				if (elements.size() > 0) { // if there is some array or function, otherwise no substitution is done	
					if (!varsInExp.contains(this.currentVar)){ // if the var is not in the expression
						continue;
					}
					
					if (decl.getDeclType() instanceof DeclarationInt) {
						Expression cond = new ExpressionLiteral(TypeInt.getInstance(), 0);
						DeclarationInt declInt = (DeclarationInt) decl.getDeclType();
						// the lowest value of the var
						Integer low = (Integer) declInt.getLow().evaluate(this.mf.getEvaluateContext());
						// the higher value
						Integer high = (Integer) declInt.getHigh().evaluate(this.mf.getEvaluateContext());
						// for any value in the interval we replace the variable for its value
						for (int j = low; j <= high; j++) {
							this.currentVal = new ExpressionLiteral(TypeInt.getInstance(), j);
							
							// for a variable x, we generate the equality x=v for the given value v,
							// this will be part of the guard
							Expression actualValue = new ExpressionBinaryOp(ExpressionBinaryOp.EQ,
									new ExpressionIdent(this.currentVar), new ExpressionLiteral(TypeInt.getInstance(), j));
							
							// we create the replacer
							ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
							
							// the new expression with the var replaced
							Expression new_exp = (Expression) e.accept(replacer);
							
							// Also we create a conditional expression that will replace the original formula
							cond = new ExpressionITE(actualValue, new_exp, cond);
							
							
						}
						fl.setFormula(i, cond);
					}
					if (decl.getDeclType() instanceof DeclarationBool) {
						DeclarationBool declBool = (DeclarationBool) decl.getDeclType();
		
						ArrayList<Boolean> bs = new ArrayList<Boolean>();
						bs.add(true);
						bs.add(false);
						for (boolean b : bs) {
							this.currentVal = new ExpressionLiteral(TypeBool.getInstance(), b);
							Expression cond = new ExpressionLiteral(TypeBool.getInstance(), false);
							// for a variable x, we generate the equality x=v for the given value v,
							// this will be part of the guard
							Expression actualValue = new ExpressionBinaryOp(ExpressionBinaryOp.EQ,
									new ExpressionIdent(this.currentVar), new ExpressionLiteral(TypeBool.getInstance(), b));
							
							// we create the replacer
							ReplaceVariable replacer = new ReplaceVariable(this.currentVar, this.currentVal);
							
							// the new expression with the var replaced
							Expression new_exp = (Expression) e.accept(replacer);
							
							// Also we create a conditional expression that will replace the original formula
							fl.setFormula(i, new ExpressionITE(actualValue, new_exp, cond));
						}//endfor
					}//endif
				}// endif
			}//end for
		}//end for
		return fl;
	}
	
	
	private  Boolean checkInconsistency(Expression e) throws PrismLangException{
		
		if (e instanceof ExpressionBinaryOp) {
			// case Boolean operators
			ExpressionBinaryOp exp = (ExpressionBinaryOp) e;
			Boolean resultop1 = false;
			Boolean resultop2 = false;
			try {
				resultop1 = exp.getOperand1().evaluateBoolean();
			}
			catch(Exception exception) {
				resultop1 = null;
				
			}
			try {
				resultop2 = exp.getOperand2().evaluateBoolean();
			}
			catch(Exception exception) {
				resultop2 = null;
			}
			if (exp.getOperator() == ExpressionBinaryOp.AND || exp.getOperator() == ExpressionBinaryOp.OR || exp.getOperator() == ExpressionBinaryOp.IMPLIES || exp.getOperator() == ExpressionBinaryOp.IFF) {
					
				switch (exp.getOperator()) {
					case ExpressionBinaryOp.AND :
						if ((resultop1 != null && resultop1 == false) ||  (resultop2 != null && resultop2 == false))
							return true;
					case ExpressionBinaryOp.OR :
						if ((resultop1 != null && resultop1 == false) &&  (resultop2 != null && resultop2 == false))
							return true;
					case ExpressionBinaryOp.IMPLIES:
						if ((resultop1 != null && resultop1 == true) && (resultop2 != null && resultop2 == false))
							return true;
					case ExpressionBinaryOp.IFF:
						if (resultop1 != null &&  resultop2 != null  && ((resultop1 == true && resultop2 == false) || (resultop1 == false && resultop2 == true)))
							return true;
					default :
				}
			
			}		
			// case <, >, =, <=, >=
			Integer resultop1Int = 0;
			Integer resultop2Int = 0;
			try {
				resultop1Int = exp.getOperand1().evaluateInt();
			}
			catch(Exception exception) {
				resultop1 = null;
				
			}
			try {
				resultop2Int = exp.getOperand2().evaluateInt();
			}
			catch(Exception exception) {
				resultop2 = null;
			}
			
			if (exp.getOperator() == ExpressionBinaryOp.EQ || exp.getOperator() == ExpressionBinaryOp.GE || exp.getOperator() == ExpressionBinaryOp.GT || exp.getOperator() == ExpressionBinaryOp.LE || exp.getOperator() == ExpressionBinaryOp.GE) {
				
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
			
			}		
			
		}
		if (e instanceof ExpressionUnaryOp) {
			ExpressionUnaryOp exp = (ExpressionUnaryOp) e;
			Boolean resultop = false;
			try {
				resultop = exp.getOperand().evaluateBoolean();
			}
			catch(Exception exception) {
				resultop = null;
			}
			if (exp.getOperator() == ExpressionUnaryOp.NOT && resultop == false){
				return true;
				
			}
		}
		
			
		return false;
		
	}
	
}
