package sootAnalyzer;

/* Soot - a J*va Optimization Framework
 * Copyright (C) 2008 Eric Bodden
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SootAnalyzer {

	public static String classpathDelimiter =";";
	public static String directoryToAnalyze = "src\\sootAnalyzer\\code";

	public static List<String> lSootClasspath = new ArrayList<String>();
	
	public static void init() {
		// System.out.println(System.getProperty("java.home"));
		System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.8.0_202\\jre");
		String parent = "C:\\Program Files\\Java\\jdk1.8.0_202\\jre";
//		lSootClasspath.add(new File(new File(parent, "lib"),
//				"jsse.jar").getPath());
		lSootClasspath.add(new File(new File(parent, "lib"),
				"rt.jar").getPath());
//		lSootClasspath.add(new File(new File(parent, "lib"),/
//				"jce.jar").getPath());
//		lSootClasspath.add(directoryToAnalyze);
	}

	public static void main(String[] args) {
		init();

		for(String file : lSootClasspath) {
			if (!new File(file).exists()) {
				throw new IllegalArgumentException(file + " does not exist");
			}
		}

		System.out.println("Analyzing "+directoryToAnalyze);
/*
		PackManager.v().getPack("jtp").add(
				new Transform("jtp.myTransform", new BodyTransformer() {

					@Override
					protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
						SootMethod sootMethod = body.getMethod();
						System.out.println(sootMethod.getSignature());

						for(Unit unit : body.getUnits()){
							Stmt statement = (Stmt) unit;
							
							if(statement instanceof JIfStmt){
								JIfStmt ifStmt = (JIfStmt) statement;
								System.out.println(ifStmt.toString());
							}
							else if(statement instanceof JAssignStmt){
								JAssignStmt assignStmt = (JAssignStmt) statement;
								System.out.println(assignStmt.toString());
							}
						}

					}


				}));
*/
		String sootClasspath = "lib\\soot-4.5.0-jar-with-dependencies.jar";
		for (String jar : lSootClasspath) { sootClasspath+=classpathDelimiter+jar;}

		String[]  myArgs =
			{
				"-cp", sootClasspath,
//				"-include-all",
//				"-allow-phantom-refs",
//				"-prepend-classpath",
				"-output-format", "J",
//				"-p","jb","use-original-names:true",
				"-process-dir", directoryToAnalyze,
    			"-debug"
			};

		System.out.println("myargs:");
		for (String arg: myArgs) {
			System.out.print(arg + " ");
		}
		System.out.println();

		soot.Main.main(myArgs);
	}
}