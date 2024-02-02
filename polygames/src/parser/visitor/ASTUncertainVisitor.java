package parser.visitor;

import parser.ast.*;
import parser.ast.Module;
import parser.type.TypeDouble;
import prism.PrismException;
import prism.PrismLangException;
import parma_polyhedra_library.Polyhedron;
import parma_polyhedra_library.Constraint_System;

import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Parma_Polyhedra_Library;
import parma_polyhedra_library.Variable;
import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.NNC_Polyhedron;
import parma_polyhedra_library.Generator_Type;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Sum;
import parma_polyhedra_library.Linear_Expression_Times;
import parma_polyhedra_library.Linear_Expression_Coefficient;

import parma_polyhedra_library.Generator;
import parma_polyhedra_library.Generator_System;
import parma_polyhedra_library.Relation_Symbol;
import explicit.PPLSupport;
import prism.PrismException;
import java.util.ArrayList;
import java.util.List;

public  class ASTUncertainVisitor extends DeepCopy{
    
	//public Expression visit(UncertainExpression e) throws PrismLangException{
    //	Expression result = null;
    //    return result;
    //}
    
	
	@Override
	public Object visit(Module e) throws PrismLangException
	{
		//return e.clone().deepCopy(this);
		
		Module result = new Module(e.getName());
		result.setInvariant(this.copy(e.getInvariant()));
		result.setNameASTElement(this.copy(e.getNameASTElement()));
		for (Declaration d : this.copyAll(e.getDeclarations())) {
			result.addDeclaration(d);
		}
		
		for (Command c : e.getCommands()) {
			for (Command cc : this.visit(c)) {
				result.addCommand(cc);
				System.out.println(cc);
			}
		}
		//Invariant invariant = copier.copy(invariant);
		//nameASTElement = copier.copy(nameASTElement);

		//copier.copyAll(decls);
		//copier.copyAll(commands);

		return result;
	}
	
	
	
	@Override
	public ArrayList<Command> visit(Command e) throws PrismLangException
	{
		ArrayList<Command> result = new ArrayList<Command>();
		
		// in the case that the update is normal
		if (!(e.getUpdates() instanceof UncertainUpdates)) {
			result.add(e.clone().deepCopy(this));
			return result;
		}
		
		// otherwise we copy everything except the update
		ArrayList<Updates> ups = (ArrayList<Updates>) this.visit((UncertainUpdates) e.getUpdates());
		for (Updates up : ups) {
			Command c = e.clone();
			c.setParent(e.getParent());
			c.setGuard(this.copy(c.getGuard()));
			c.setUpdates(up);
			result.add(c);
		}
		
		return result;
	}
	
	@Override
    public ArrayList<Updates> visit(UncertainUpdates e) throws PrismLangException{
    	
		if (e.getNumberUncertains() == 0)
			throw new PrismLangException("Error computing the vertices of the equations");
		
    	ArrayList<Updates> result = new ArrayList<Updates>();
    	//result.setParent(e.getParent()); // we set the parent 
    	
    	// We init PPL
    	try {
			System.loadLibrary("ppl_java");
			Parma_Polyhedra_Library.initialize_library();
		} catch (UnsatisfiedLinkError e1) {
			System.err.println("\nError loading Parma Polyhedra Library:");
			System.err.println(e1);
			throw new PrismLangException("Parma Polyhedra Library could not be loaded/initialised");
		} catch (Exception e2) {
			System.err.println("\nError loading Parma Polyhedra Library:");
			System.err.println(e2);
			throw new PrismLangException("Parma Polyhedra Library could not be loaded/initialised");
		}
    	
    	e.convertToInt(); // all have to be Int, this function converts all the constants to Int
    	NNC_Polyhedron ph = new NNC_Polyhedron(e.getPPLConstraintSystem());
    	
    	System.out.println(e.getPPLConstraintSystem());
    	
    	// We check that the polyhedral is bounded
    	// the polyhedra is always bounded beacuse the added restrictions
    	//if (!ph.is_bounded())
    	//	throw new PrismLangException("A polyhedral is not bounded.");
    	
    	Generator_System gs = ph.generators();
    	int div = e.getDivisor();
    	//System.out.println(ph.is_bounded());
    	for (Generator g : gs) {
    		try {
    			//System.out.println(PPLSupport.getGeneratorAsVector(g, e.getNumberUncertains()));
    			Updates ups = new Updates();
    			List<Double> vertices = PPLSupport.getGeneratorAsVector(g, e.getNumberUncertains());
    			for (int i = 0; i < vertices.size(); i++) {
    				//Updates ups = new Updates();
        			ups.addUpdate(new ExpressionLiteral(TypeDouble.getInstance(), vertices.get(i), vertices.get(i).toString()), e.getUpdate(i));
        			ups.setParent(e.getParent());		
    			}
    			result.add(ups);
    		}
    		catch(Exception exp) {
    			throw new PrismLangException("Error computing the vertices of the equations");
    		}
    	}	
    	//System.out.println(result);
    	return result;
    }
    
    
}
