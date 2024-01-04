/******************************************************************************
 * Copyright (c) 2023-
 * Authors:
 * Pablo Castro <pcastro@dc.exa.unrc.edu.ar> (National University of Rio Cuarto - Argentina)
 * Axel Bassegio <abassegio@dc.exa.unrc.edu.ar> (National University of Rio Cuarto - Argentina)
 *
 * This file is part of PolyGames
 *
 * PolyGames is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * FairyGames is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FairyGames; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * This class provides the basic behavior of the command line tool for polygames
 */

package poly;

import java.io.File;
import java.io.FileNotFoundException;


import prism.PrismModelListener;
import prism.PrismLog;
import prism.PrismFileLog;
import prism.PrismException;
import prism.Prism;
import parser.ast.ModulesFile;
import parser.visitor.ASTUncertainVisitor;
import prism.ModelType;



public class PolyCL implements PrismModelListener{
	
	// basic filenames
	private String mainLogFilename = "stdout";
	private String settingsFilename = null;
	private String modelFilename = "";
		
	// params info
	private String[] paramLowerBounds = null;
	private String[] paramUpperBounds = null;
	private String[] paramNames = null;
		
	// main log
	private PrismLog mainLog = null;
	private boolean checkFairness = false;
	private boolean computeRewards = false;
	private String constSwitch = null; // for  dealing with constants
	private boolean exportResults = false; // true if export is enabled
	private String exportResultsFilename = null;
	
	// model failure info
	boolean modelBuildFail = false;
	Exception modelBuildException = null;
		
	// prism object
	Poly prism = null;
	
	
	@Override
	public void notifyModelBuildSuccessful(){
	}

	@Override
	public void notifyModelBuildFailed(PrismException e){
		modelBuildFail = true;
		modelBuildException = e;
	}
	
	public void run(String[] args){
		// we only parse the arguments for now.
		
		// deals with the arguments
		parseArgs(args);
		ModulesFile modulesFile = null;
		try {
			// we initialize everything perhaps other methods for this will be better
			this.mainLog = new PrismFileLog("stdout");
		
			// create prism object(s)
			this.prism = new Poly(mainLog);
			prism.addModelListener(this);
		
			// initialise
			prism.initialise();
			prism.setEngine(Prism.EXPLICIT); 
		}
		catch (PrismException e) {
			errorAndExit(e.getMessage());
		}
		
		// the file with the model is parsed
		try {
			mainLog.print("\nParsing model file \"" + modelFilename + "\"...\n");
			modulesFile = prism.parseModelFile(new File(modelFilename), null);	
			ASTUncertainVisitor visitor = new ASTUncertainVisitor();
			ModulesFile mf = visitor.copy(modulesFile);
			System.out.println(mf);
			prism.loadPRISMModel(modulesFile);
		}catch (FileNotFoundException e) {
			errorAndExit("File \"" + modelFilename + "\" not found");
		}catch (PrismException e) {
			errorAndExit(e.getMessage());
		}
		
		// check if model type is correct
		if (!((prism.getModelType() == ModelType.SMG) || (prism.getModelType() == ModelType.STPG))){
			errorAndExit("Model type must be SMG or STPG");
		}
		
		
	}
	
	/**
	 * A basic handler for the args, it must be improved, for now it only accepts the -help option
	 * @param args
	 */
	private void parseArgs(String[] args){
		
		constSwitch = "";
		
		for (int i=0; i<args.length; i++){
			switch(args[i]){
			case "-help": 
				printHelp();
				System.exit(0);
				break; // little sense never reached...
			default:
				if (this.modelFilename.equals("")) // in this case it must be the file name
					this.modelFilename = args[i];
				else { // otherwise it is a unrecognized option
					System.out.println("Proper Usage is: java poly [options] <model filename>");
					System.exit(0);
				}
			}
		}
		// at this point the argument were parsed.
	}
	
	private void printHelp() {
		
		System.out.println("Proper Usage is: java poly [options] <model filename>");
		System.out.println("where [options] are:");
		System.out.println("-help: prints this help.");
	}
	
	/**
	 * Report a (fatal) error and exit cleanly (with exit code 1).
	 */
	private void errorAndExit(String s)
	{
		prism.closeDown(false);
		mainLog.println("\nError: " + s + ".");
		mainLog.flush();
		System.exit(1);
	}

	/**
	 * Exit cleanly (with exit code 0).
	 */
	private void exit()
	{
		prism.closeDown(true);
		System.exit(0);
	}

	// pretty simple main, it only runs fairy
	public static void main(String[] args){
		new PolyCL().run(args);
	}
	

}
