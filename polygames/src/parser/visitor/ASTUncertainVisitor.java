package parser.visitor;

import parser.ast.*;
import parser.ast.Module;
import parser.type.TypeDouble;
import prism.PrismLangException;
import parma_polyhedra_library.NNC_Polyhedron;

import parma_polyhedra_library.Generator;
import parma_polyhedra_library.Generator_System;
import explicit.PPLSupport;
import java.util.ArrayList;
import java.util.List;

public class ASTUncertainVisitor extends DeepCopy {
	@Override
	public Object visit(Module e) throws PrismLangException {
		Module result = new Module(e.getName());
		result.setInvariant(this.copy(e.getInvariant()));
		result.setNameASTElement(this.copy(e.getNameASTElement()));
		for (Declaration declaration : this.copyAll(e.getDeclarations())) {
			result.addDeclaration(declaration);
		}

		for (Command command : e.getCommands()) {
			for (Command cc : this.visit(command)) {

				result.addCommand(cc);
			}
		}

		return result;
	}

	@Override
	public ArrayList<Command> visit(Command e) throws PrismLangException {
		ArrayList<Command> result = new ArrayList<Command>();

		// in the case that the update is normal

		if (!(e.getUpdates() instanceof UncertainUpdates)) {
			result.add(e.clone().deepCopy(this));

			return result;
		}

		// otherwise we copy everything except the update
		ArrayList<Updates> updates = (ArrayList<Updates>) this.visit((UncertainUpdates) e.getUpdates());
		for (Updates update : updates) {
			Command command = e.clone();

			command.setParent(e.getParent());
			command.setGuard(this.copy(command.getGuard()));
			command.setUpdates(update);

			result.add(command);
		}

		return result;
	}

	@Override
	public List<Updates> visit(UncertainUpdates e) throws PrismLangException {
		if (e.getNumberUncertains() == 0) {
			throw new PrismLangException("Error computing the vertices of the equations");
		}

		List<Updates> result = new ArrayList<Updates>();

		try {
			PPLSupport.initPPL();
		} catch (Exception exception) {
			System.err.println("Error loading Parma Polyhedra Library:");
		}

		e.convertToInt();

		NNC_Polyhedron ph   = new NNC_Polyhedron(e.getPPLConstraintSystem());
		Generator_System gs = ph.generators();

		for (Generator g : gs) {
			try {
				Updates updates       = new Updates();
				List<Double> vertices = PPLSupport.getGeneratorAsVector(g, e.getNumberUncertains());
				for (int i = 0; i < vertices.size(); i++) {
					if (vertices.get(i) != 0) {
						updates.addUpdate(
							new ExpressionLiteral(TypeDouble.getInstance(), vertices.get(i), vertices.get(i).toString()), e.getUpdate(i)
						);
						updates.setParent(e.getParent());
					}
				}

				result.add(updates);
			} catch (Exception exp) {
				throw new PrismLangException("Error computing the vertices of the equations");
			}
		}

		return result;
	}
}
