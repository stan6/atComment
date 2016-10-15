package javadocAnalyzer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Character;
import javadocAnalyzer.Counters;
import java.util.Random;
import com.sun.javadoc.*;


public class NullParam extends Doclet {

	static Hashtable<Parameter, ParamTag> paramTagTable = new Hashtable<Parameter, ParamTag>();
	static int illegalArg = 0;
	static int nullPointer = 0;
	static int otherExcep = 0;
	static HashMap<String, Integer> exceptionMap;
	static Hashtable<String, NullStatus> nullTable;
	static String[] randomArr = new String[130];
	static int curr=0;
	/**
	 * This is the starting point of all Javadoc Doclet
	 * @param root
	 * @return
	 */
	public static boolean start(RootDoc root) {
		exceptionMap = new HashMap<String, Integer>();
		int pos = 0;
		String classList = "";

		for (ClassDoc cd : root.classes()) {

			if (!cd.name().contains(".")) {
				classList += cd.qualifiedName() + "\n";

				if (findSourceFile(root) != null) {
					if (pos == 0) {
						File f = new File(getInferNullPath(root, cd));
						//System.out.println("dir:"+getInferNullPath(root, cd));
						if (!f.exists()){
							f.mkdir();
						}	
					}
					//process and generate .infer string
					processRoot(root, cd, false);
					
					//process and generate .jml string
					//processRoot(root, cd, true);

					pos++;
				}
			}

		}
                Counters.printAll();
		/*for(int i=0;i<randomArr.length;i++){
			if(randomArr[i]!=null)
				System.out.println(i+"."+randomArr[i]);
		}*/
		return true;
	}

	/**
	 * Get the string for generating .infer or .jml and write it to a path
	 * @param root
	 * @param cd
	 * @param useJml decide if the string processed is for generating .infer or .jml
	 */
	@SuppressWarnings("deprecation")
	private static void processRoot(RootDoc root, ClassDoc cd, boolean useJml) {
		
		String path = JMLOrInferPath(root, cd, useJml);
		String toBeWritten = "";
		toBeWritten += "package " + cd.containingPackage().name()
				+ ";\n\n";
		ClassDoc[] importClasses = cd.importedClasses();
		for (ClassDoc importCls : importClasses) {
			toBeWritten += "import " + importCls + ";\n";
		}
		PackageDoc[] importPackages = cd.importedPackages();
		for (PackageDoc importPackage : importPackages) {
			toBeWritten += "import " + importPackage + ".*;\n";
		}
		toBeWritten += "\n" + getClassSignature(cd);
		toBeWritten += getParamComment(path, cd.constructors(),
				true, useJml);
		toBeWritten += getParamComment(path, cd.methods(), false,
				useJml);
	
		for (ClassDoc inner : cd.innerClasses()) {
			if(!inner.name().equals(cd.name()) && cd.name().equals(path)){
			toBeWritten += "\n" + getClassSignature(inner);
			toBeWritten += getParamComment(path,
					inner.constructors(), true, useJml);
			toBeWritten += getParamComment(path, inner.methods(),
					false, useJml);
			toBeWritten += "\n}";
			}

		}
		toBeWritten += "\n}";
		writeToPath(path, toBeWritten);
	}

	/**
	 * parse and return the information about a class including the interfaces
	 * it extend, its superclass and the modifier
	 * 
	 * @param cd
	 * @return String containing the class signature
	 */
	private static String getClassSignature(ClassDoc cd) {
		String toBeWritten = "";
		int mods = cd.modifierSpecifier();

		if (mods > 0)
			toBeWritten += cd.modifiers() + " ";
		if (!cd.isInterface()) // interface is a modifier
			/*
			 * to simplify innerclass name ie. to change outerclass.innerclass
			 * to innerclass
			 */
			if (cd.name().contains(".")) {
				toBeWritten += "class "
						+ cd.name().substring(cd.name().lastIndexOf(".") + 1,
								cd.name().length());
			} else {
				toBeWritten += "class " + cd.name();
			}
		else {
			if (cd.name().contains(".")) {
				toBeWritten += cd.name().substring(
						cd.name().lastIndexOf(".") + 1, cd.name().length());
			} else {
				toBeWritten += cd.name();
			}
		}

		boolean firstImplements = true;
		boolean firstExtends = true;
		Type supercl = cd.superclassType();
		if (supercl != null) {
			toBeWritten += " extends " + supercl.simpleTypeName();
			firstExtends = false;
		}
		ClassDoc[] interfaces = cd.interfaces();

		for (int k = 0; k < interfaces.length; k++) {
			if (interfaces[k].isInterface() && cd.isInterface() && firstExtends) {

				toBeWritten += " extends ";
				firstExtends = false;
			} else if (interfaces[k].isInterface() && cd.isClass()
					&& firstImplements) {
				toBeWritten += " implements ";
				firstImplements = false;
			}

			if (k > 0)
				toBeWritten += ", ";
			toBeWritten += interfaces[k].name();
		}

		toBeWritten += " {\n\n";

		return toBeWritten;
	}

	private static String JMLOrInferPath(RootDoc root, ClassDoc cd, boolean jml) {
		String path = "";
		if (jml) {
			if(!getInferNullPath(root, cd).endsWith(System.getProperty("file.separator")))
				path = getInferNullPath(root, cd) + System.getProperty("file.separator") + cd.name() + ".jml";
			else
				path = getInferNullPath(root, cd) + cd.name() + ".jml";
		} else {
			if(!getInferNullPath(root, cd).endsWith(System.getProperty("file.separator")))
				path = getInferNullPath(root, cd) + System.getProperty("file.separator") + cd.name() + ".infer";
			else
				path = getInferNullPath(root, cd) + cd.name() + ".infer";
		}
		return path;
	}

	/**
	 * Returns the sourcepath entered by the user at the command line
	 * 
	 * @param root
	 * @return
	 */
	private static String findSourceFile(RootDoc root) {
		String[][] options = root.options();
		for (int row = 0; row < options.length; row++) {
			if (options[row][0].equals("-sourcepath")) {
				if (options[row].length == 2) {
					String srcpath=options[row][1];
					//get the path for the src by finding the word src
					int beforeslash=srcpath.lastIndexOf("src");
					if(beforeslash==-1)
						beforeslash=srcpath.lastIndexOf("source");
					//for path without src as the string
					if(beforeslash==-1)
						return srcpath;
					return srcpath.substring(0,beforeslash);
				}
			}
		}
		return null;
	}

	private static String getInferNullPath(RootDoc root, ClassDoc cd){
		if(!findSourceFile(root).endsWith(System.getProperty("file.separator")))
			return findSourceFile(root) + System.getProperty("file.separator")+ "inferNull";
		else
			return findSourceFile(root) + "inferNull";
	}

	static void writeToFile(String path, String content) throws IOException {
		// Create file
		BufferedWriter out = new BufferedWriter(new FileWriter(path, false));
		try {
			out.append(content);
		} finally {
			out.close();
		}
	}

	/**
	 * Analyze all the comments related to the parameters for a method
	 * 
	 * @param method
	 * @param isConstructor
	 */
	private static String getParamComment(String path,
			ExecutableMemberDoc[] method, boolean isConstructor, boolean useJML) {
		String methodInfer = "";
		if(!useJML){
			countMethodStat(method);	
		}
		for (ExecutableMemberDoc emd : method) {
			nullTable = new Hashtable<String, NullStatus>();
			Parameter[] param = emd.parameters();
			ParamTag[] paramTag = emd.paramTags();
			ThrowsTag[] throwTags = emd.throwsTags();
			List<NullStatus> statusList=new LinkedList<NullStatus>();
			if (!useJML){
						statusList = matchParamWithTag(emd, param,
								paramTag);
			
						matchThrowsWithParam(statusList, emd, param, throwTags);
			}

			String retStr = null;
			if (!useJML) {
				retStr = printNullStatusInfer(statusList, emd);
				
			} else {
				retStr = printNullStatusJML(statusList, emd);
			}
			methodInfer += retStr;
		}
		return methodInfer;
	}

	/**
	 * 
	 * @param method
	 */
	private static void countMethodStat(ExecutableMemberDoc[] method) {
		outerloop:
		for (ExecutableMemberDoc emd : method) {
			
			if(!emd.commentText().isEmpty()){
				Counters.methodBlock++;
				//System.out.println("comment text:"+emd.commentText());
				continue outerloop;
			}
 			
			for(ParamTag paramTag:emd.paramTags()){
				if(!paramTag.parameterComment().isEmpty()){
					Counters.methodBlock++;
					//System.out.println("param text:"+paramTag.parameterComment());
					continue outerloop;
				}
			}
			for(ThrowsTag throwsTag:emd.throwsTags()){
				if(!throwsTag.exceptionComment().isEmpty()){
					Counters.methodBlock++;
					//System.out.println("throw text:"+throwsTag.exceptionComment());
					continue outerloop;
				}
			}
			
		}	
		outer2:
		for (ExecutableMemberDoc emd : method) {
			
			if(emd.commentText().contains("null")){
				Counters.methodMentionNull++;
				//System.out.println("null comment text:"+emd.commentText());
				continue outer2;
			}
			
			for(ParamTag paramTag:emd.paramTags()){
				if(paramTag.parameterComment().contains("null")){
					Counters.methodMentionNull++;
					//System.out.println("null param text:"+paramTag.parameterComment());
					continue outer2;
				}
			}
			for(ThrowsTag throwsTag:emd.throwsTags()){
				if(throwsTag.exceptionComment().contains("null")){
					Counters.methodMentionNull++;
					//System.out.println("null throw text:"+throwsTag.exceptionComment());
					continue outer2;
				}
			}
		}
	}

	private static void writeToPath(String path, String retStr) {
		if (path != null) {
			try {
				writeToFile(path, retStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static String getParamList(Parameter[] params) {
		String temp = "(";
		int pos = 0;
		for (Parameter param : params) {
			String typeName = param.typeName();
			// to get rid of type like org.eclipse.swt.graphics.RGB[]
			int dotLoc = findDotLoc(typeName);

			if (dotLoc != -1) {
				typeName = typeName.substring(dotLoc + 1);
			}

			temp = temp + typeName + " " + param.name();
			// Add comma except for the last parameter
			if (pos < params.length - 1) {
				temp += ",";
			}
			pos++;
		}
		temp += ")";
		return temp;
	}

	private static String printNullStatusInfer(List<NullStatus> statusList,
			ExecutableMemberDoc method) {
		
		String temp = "";
		//temp= printJavadoc(method,temp);
		//temp=getMethodSignatureInfer(method, temp);
		//temp+="---------------------------------------------------------\n";
		
		int currParam=0;
		double totalProp=0;
		Random generator = new Random();
		int randomIndex = generator.nextInt(10);
		String tempCurr=null;
		if(randomIndex<=300 && curr<=129 && (method.paramTags().length>0 || method.throwsTags().length>0)){
			int randomTags = generator.nextInt(method.paramTags().length+method.throwsTags().length);
					
					
					boolean nonprim=false;
					if(randomTags<method.paramTags().length){
						for(Parameter param : method.parameters()){
							if(param.name().equals(method.paramTags()[randomTags].parameterName())){
								String typeName = param.typeName();
								if(!isPrimitive(typeName)){
									nonprim=true;
								}
							}
						}
					}
					if(nonprim && randomTags<method.paramTags().length){
						tempCurr="Original tag "+method.paramTags()[randomTags]+ " for "+method.name()+method.flatSignature()+"\n";
					}else if(randomTags-method.paramTags().length>=0){
						tempCurr="Original tag "+method.throwsTags()[randomTags-method.paramTags().length]+ " for "+method.name()+method.flatSignature()+"\n";
						if(!method.throwsTags()[randomTags-method.paramTags().length].exceptionComment().contains("null")){
							boolean proceed=false;
							for(ParamTag paramTag:method.paramTags()){
								if(paramTag.parameterComment().contains("null"))
									proceed=true;
								tempCurr+="param: "+paramTag+ "\n";
							}
							if(!proceed)
								tempCurr=null;
						}
					}
						
		}
		for (NullStatus status : statusList) {
			for (Parameter param : method.parameters()) {
				String paramName = "";
				if (status.getParamName() != null) {
					paramName = status.getParamName()
							.replace(method.name(), "");
					String typeName = param.typeName();
					int dotLoc = findDotLoc(typeName);

					if (dotLoc != -1) {
						typeName = typeName.substring(dotLoc + 1);
					}
					if(param.name().compareToIgnoreCase(paramName) == 0 && isPrimitive(typeName)){
						temp += "@Unknown(" + param.name() + ")\n";		
					}
					if (param.name().compareToIgnoreCase(paramName) == 0
							&& !isPrimitive(typeName)) {
						/*This is the code introduced for annotation @OtherException:	
						expected exceptions that are not related to null, not parameter specified*/
						if (status.getExpectedException().size() > 0){
							for(String exception : status.getExpectedException()){
								String nullRelated=":null(";
								
								if(exception.startsWith(nullRelated)){
									String name=exception.substring(nullRelated.length());
									name=name.substring(0,name.indexOf(")"));
									CharSequence cs = "@YesNull(" + name + "==null =>" + exception.substring(nullRelated.length()+name.length()+1) + ")\n";
									if (!temp.contains(cs))
										temp += "@YesNull(" + name + "==null =>" + exception.substring(nullRelated.length()+name.length()+1) + ")\n";
								}
								else{
								CharSequence cs = "@OtherException("+exception+")";
								if (!temp.contains(cs))
									temp += "@OtherException("+ exception + ")\n";
								}
							}
					
						}
						// The case where the parameter cannot be null according
						// to
						// @param can be null according to @throw. Then, the
						// parameter is annotated with @YesNull together
						// with the exception inferred. Or if @param doesn't
						// provided any information.
						if ((status.getParamNull() == 0
								|| status.getParamNull() == -1 || status
								.getParamNull() == 2)
								&& status.getThrowIfNull() == 1) {
							CharSequence cs ="@YesNull("
									+ status.getThrowImplication().replace(
											method.name(), "") + ")\n";
							if (!temp.contains(cs))
								temp += "@YesNull("
									+ status.getThrowImplication().replace(
											method.name(), "") + ")\n";
							Counters.yesNullThrow++;
							totalProp++;
							
								if(tempCurr!=null){
									tempCurr+=": "+"@YesNull("
										+ status.getThrowImplication().replace(
												method.name(), "") + ")\n";
								}								
						}
						// the case where the parameter cannot be null according
						// to
						// @param and @throw does not provided any information.
						// Then,
						// the parameter is annotated with @NonNull(paramNamme)
						else if ((status.getParamNull() == 0)
								&& (status.getThrowIfNull() == -1 || status
										.getThrowIfNull() == 2)) {
							temp += "@NonNull(" + paramName + ")\n";
							Counters.nonNull++;
							totalProp++;
							if(tempCurr!=null){
								tempCurr+=": "+"@NonNull(" + paramName + ")\n";
							}
							
						}
						

						// the case where the parameter can be null according to
						// @param. Then, the parameter is annotated with
						// @YesNull(paramName)
						else if (status.getParamNull() == 1) {
							temp += "@YesNull(" + paramName + ")\n";
							Counters.yesNull++;
							totalProp++;
							if(tempCurr!=null){
								tempCurr+=": "+"@YesNull(" + paramName + ")\n";
							}
							
						}

						else if ((status.getParamNull() == -1 || status
								.getParamNull() == 2)
								&& status.getThrowIfNull() == -1
								|| status.getThrowIfNull() == 2) {
							temp += "@UnknownNull(" + paramName + ")\n";
							Counters.unknownNull++;
							totalProp++;
							
							
							if(tempCurr!=null){
								tempCurr+=": "+"@UnknownNull(" + paramName + ")\n";
							}
						}
					}
					
				}
			
				currParam++;
				
			}
			
		}
		if(tempCurr!=null){
							randomArr[curr]=tempCurr;
							curr++;
						}
	       	
		int totalParameters=method.parameters().length;
		int totNonPriParameters=0;
		double percentage=100;
		double percentPri=100;
		for(Parameter param:method.parameters()){
			String typeName = param.typeName();
			if (param.typeName().indexOf(".") != -1) {
				typeName = param.typeName().substring(param.typeName().lastIndexOf(".") + 1);
			}
			
			if(!isPrimitive(typeName)){
				totNonPriParameters++;
			}
		}

		countPercentagePri(method, totalProp, totalParameters,
				totNonPriParameters, percentage, percentPri);
		temp = getMethodSignatureInfer(method, temp);
		temp+="\n\n";
		return temp;
	}

	/**
	 * Used for counting percentage of primitive parameter covered 
	 * @param method
	 * @param totalProp
	 * @param totalParameters
	 * @param totNonPriParameters
	 * @param percentage
	 * @param percentPri
	 */
	private static void countPercentagePri(ExecutableMemberDoc method,
			double totalProp, int totalParameters, int totNonPriParameters,
			double percentage, double percentPri) {
		//to avoid division by zero,100 percentage by default since if there is no param tags,
		//the overall percentage shouldn't be affected 
		if(totalParameters>0)		
			percentage=(totalProp/totalParameters)*100;
		if(totNonPriParameters>0)		
			percentPri=(totalProp/totNonPriParameters)*100;
		//set both percentage to 100.0 if the method doesn't contains any param tags and throws tags
		if(method.paramTags().length==0 && method.throwsTags().length==0){
			percentage=100.0;
			percentPri=100.0;
		}
		//if(percentage<100.00)
		//	System.out.println("method:"+method.name()+":"+percentage+":totalPro:"+totalProp+":parameters:"+totalParameters);
		//if(percentPri<100.00)
		//	System.out.println("pri method:"+method.name()+":"+percentPri+":totalPro:"+totalProp+":nonpri parameters:"+totNonPriParameters);
		Counters.totMethods++;
		Counters.totMethodsCovered+=percentage;	
		Counters.totMethodsNonPriCovered+=percentPri;
	}

	private static String printJavadoc(ExecutableMemberDoc method,String temp) {
		//this block is for manual inspection
		temp+="/* "+method.commentText()+" */\n";
		for(ParamTag paramTag : method.paramTags()){
			temp+="@param "+paramTag.parameterName()+" "+paramTag.parameterComment()+"\n";
		}
	
		for(ThrowsTag throwTag : method.throwsTags()){
			temp+="@throws "+throwTag.exceptionName()+" "+throwTag.exceptionComment()+"\n";
		}
		return temp;
	}

	private static String getMethodSignatureInfer(ExecutableMemberDoc method,
			String temp) {
		// print method signature
		if (method.isMethod()) {
			MethodDoc met = (MethodDoc) method;
			temp += met.modifiers() + " " + met.returnType().simpleTypeName()
					+ met.returnType().dimension() + " " + met.name();
		} else if (method.isConstructor()) {
			ConstructorDoc met = (ConstructorDoc) method;
			temp += met.modifiers() + " " + met.name();
		}
		String paramList = getParamList(method.parameters());
		temp += paramList + "\n";
		return temp;
	}

	private static int findDotLoc(String typeName) {
		int dotLoc = -1;
		for (int pos = typeName.length() - 1; pos >= 0; pos--) {
			char dot = '.';
			if (typeName.charAt(pos) == dot) {
				dotLoc = pos;
				break;
			}
		}
		return dotLoc;
	}

	private static String printNullStatusJML(List<NullStatus> statusList,
			ExecutableMemberDoc method) {

		String temp = "";
		boolean nullableSet = false;
		boolean nonNullSet = false;
		String orParam = "";
		String exceptionName = "";
		int currPos = 0;
		for (NullStatus status : statusList) {

			String paramName = "";

			if (status.getParamName() != null) {
				paramName = status.getParamName().replace(method.name(), "");

				// The case where the parameter cannot be null according to
				// @param
				// can be null according to @throw. Then, the parameter is
				// annotated
				// with @YesNull together with the exception inferred. Or if
				// @param
				// doesn't provided any information.
				if (status.getThrowIfNull() == 1
						&& (status.getParamNull() == 0
								|| status.getParamNull() == -1 || status
								.getParamNull() == 2)) {
					if (status.getThrowImplication() != null) {
						exceptionName = status.getThrowImplication();
						if (exceptionName.indexOf("=>") != -1) {
							int start = exceptionName.indexOf(">");
							exceptionName = exceptionName.substring(start + 1);
						}
					}
					if (currPos == statusList.size() - 1) {
						temp += "/*@public exceptional_behavior\n"
								+ "@   requires " + orParam + paramName
								+ "==null;\n" + "@   signals_only "
								+ exceptionName + ";\n" + "@*/\n";
					} else {
						orParam += paramName + "==null || ";
					}
				}
				// the case where the parameter cannot be null according to
				// @param
				// and @throw does not provided any information. Then, the
				// parameter is
				// annotated with @NonNull(paramName)
				else if ((status.getParamNull() == 0)
						&& (status.getThrowIfNull() == -1 || status
								.getThrowIfNull() == 2)) {
					nonNullSet = true;
					// temp += "//@ requires "+paramName+"!=null;\n";
				}

				// the case where the parameter can be null according to @param.
				// Then, the parameter is annotated with @YesNull(paramName)
				else if (status.getParamNull() == 1) {
					nullableSet = true;
				}

				else if ((status.getParamNull() == -1 || status.getParamNull() == 2)
						&& status.getThrowIfNull() == -1
						|| status.getThrowIfNull() == 2) {
					// print nothing for unknown null for now
					if (currPos == statusList.size() - 1 && !orParam.isEmpty()) {

						if (orParam.endsWith("|| ")) {
							orParam = orParam.substring(0,
									orParam.lastIndexOf("|| ") - 1);
						}

						temp += "/*@public exceptional_behavior\n"
								+ "@   requires " + orParam + ";\n"
								+ "@   signals_only " + exceptionName + ";\n"
								+ "@*/\n";
					}
				}
			}
			currPos++;
		}

		temp = printMethodSigJML(statusList, method, temp, nullableSet,
				nonNullSet);

		return temp;
	}

	/**
	 * Print method signature for the parameter method given the status list.
	 * @param statusList needed since notation like nullable is embedded within method signature
	 * @param method
	 * @param temp
	 * @param nullableSet
	 * @param nonNullSet
	 * @return
	 */
	private static String printMethodSigJML(List<NullStatus> statusList,
			ExecutableMemberDoc method, String temp, boolean nullableSet,
			boolean nonNullSet) {
		// print method signature
		if (method.isMethod()) {
			MethodDoc met = (MethodDoc) method;
			temp += met.modifiers() + " " + met.returnType().simpleTypeName()
					+ met.returnType().dimension() + " " + met.name();
		} else if (method.isConstructor()) {
			ConstructorDoc met = (ConstructorDoc) method;
			temp += met.modifiers() + " " + met.name();
		}
		if (!nullableSet && !nonNullSet) {
			String paramList = getParamList(method.parameters());
			temp += paramList + ";\n\n";
		} else {
			temp += "(";
			int pos = 1;
			for (NullStatus status : statusList) {

				for (Parameter param : method.parameters()) {

					if (status.getParamName() != null) {
						String paramName = status.getParamName().replace(
								method.name(), "");
						String typeName = param.typeName();
						// to get rid of type like
						// org.eclipse.swt.graphics.RGB[]
						int dotLoc = findDotLoc(typeName);

						if (dotLoc != -1) {
							typeName = typeName.substring(dotLoc + 1);
						}

						if (param.typeName().indexOf(".") != -1) {
							typeName = param.typeName().substring(
									param.typeName().lastIndexOf(".") + 1);
						}

						// check if the keyword nullable needs to be added
						if (paramName.equals(param.name())
								&& status.getParamNull() == 1) {

							// since primitive cannot be nullable,check it first
							if (!isPrimitive(typeName)) {
								temp += "/*@nullable*/" + typeName + " "
										+ param.name();
							} else {
								temp += typeName + " " + param.name();
							}
							// Add comma except for the last parameter
							if (method.parameters().length > 1
									&& pos < method.parameters().length) {
								temp += ",";
							}
							pos++;
						} else if (paramName.equals(param.name())
								&& (status.getParamNull() == 0)
								&& (status.getThrowIfNull() == -1 || status
										.getThrowIfNull() == 2)) {
							if (!isPrimitive(typeName)) {
								temp += "/*@non_null*/" + typeName + " "
										+ param.name();
							} else {
								temp += typeName + " " + param.name();
							}
							// Add comma except for the last parameter
							if (method.parameters().length > 1
									&& pos < method.parameters().length) {
								temp += ",";
							}
							pos++;
						} else if (paramName.equals(param.name())
								&& pos <= method.parameters().length) {
							temp += typeName + " " + param.name();
							// Add comma except for the last parameter
							if (method.parameters().length > 1
									&& pos < method.parameters().length) {
								temp += ",";
							}
							pos++;
						}
					}
				}
			}
			if (temp.endsWith(",")) {
				temp = temp.substring(0, temp.length() - 1);
			}
			temp += ");\n\n";
		}
		return temp;
	}

	private static boolean isPrimitive(String typeName) {
		if (typeName.equals("int") || typeName.equals("float")
				|| typeName.equals("double") || typeName.equals("short")
				|| typeName.equals("long") || typeName.equals("byte")
				|| typeName.equals("boolean"))
			return true;
		return false;
	}

	/**
	 * Locate the parameter name in the throw comment
	 * 
	 * @param statusList
	 * @param emd
	 * @param param
	 */
	private static void matchThrowsWithParam(List<NullStatus> statusList,
			ExecutableMemberDoc method, Parameter[] params,
			ThrowsTag[] throwTags) {

		int statusIndex = 0;
		String expectedExp="";
		
		for (ThrowsTag throwTag : throwTags) {

			String throwComment = throwTag.exceptionComment();
			boolean match = false;
			String throwAllLower = throwComment.toLowerCase();
			String exceptionName = throwTag.exceptionName();

			
			if (throwAllLower == null || throwComment.isEmpty()) {
				continue;
			} 
			
			
			for(int pos=0;pos<params.length;pos++){
				if(!throwAllLower.contains("null") || !throwAllLower.contains(params[pos].name().toLowerCase())){
					//System.out.println("throw:"+"method"+method.containingClass()+":"+method.name()+":"+throwAllLower);
					printImplication(statusList.get(pos),
									exceptionName, "",
									-1);
				}else if(throwAllLower.contains("null") && (throwAllLower.contains(params[pos].name().toLowerCase()) || throwAllLower.contains("either"))){

					printImplication(statusList.get(pos),
									":null("+params[pos].name()+")"+exceptionName, "",
									-1);
				}
			}
			List<String> subjTable = SentenceParser.analyzeIf(
					params, throwAllLower, getMethodName(method));
			int i=0;
			while( i < params.length && i < subjTable.size()) {
					match = true;
					String sub=subjTable.get(i).toLowerCase();
					if (throwAllLower.contains(" or ")){
						sub="or";
					}
					else if(throwAllLower.contains("either")) {
						sub="either";
					}
					int analyzeResult = analyzeNull(sub,
							throwAllLower);
					String combine = combineMethodNameAndParam(method,
							subjTable.get(i));
					
					if (statusIndex < params.length && (params[i].name().contains(subjTable.get(i)) || params[i].typeName().toLowerCase().contains(subjTable.get(i).toLowerCase()))) {

						printImplication(statusList.get(statusIndex),
								exceptionName, combine,
								analyzeResult);
						if (analyzeResult != -1){
							statusIndex++;
						}
					}
					/*else{
						printImplication(statusList.get(i),
									exceptionName, combine,
									-1);
					}*/
					i++;			
			}

		}
	}

	private static List<NullStatus> matchParamWithTag(
			ExecutableMemberDoc method, Parameter[] params, ParamTag[] paramTags) {
		List<NullStatus> statusList = new LinkedList<NullStatus>();
		int statusInd = 0;
		int index=0;
		
		while(index<params.length)
		{
			int tagIndex=0;
			while(tagIndex<paramTags.length){
			if(paramTags[tagIndex].parameterName().equals(params[index].name())){
				String comment = paramTags[tagIndex].parameterComment();
				if(!isPrimitive(params[index].typeName()) && !comment.isEmpty()){
					//System.out.println(Counters.paramTagsAnalyzedPri+" param:"+method.containingClass()+":"+method.name()+"("+method.flatSignature()+"#"+params[index].name()+":"+paramTags[index].parameterComment());
					Counters.paramTagsAnalyzedPri++;
						}
				if(!isPrimitive(params[index].typeName()) && comment.contains("null")){
					Counters.paramNoPriNull++;
						}
				/*if(isPrimitive(params[index].typeName()) && comment.contains("null")){
					System.out.println(Counters.paramTagsAnalyzedPri+" param:"+method.containingClass()+":"+method.name()+"("+method.flatSignature()+"#"+params[index].name()+":"+paramTags[index].parameterComment());
					//Counters.paramNoPriNull++;
						}*/
			}
			tagIndex++;
			}
			index++;
		}
		
		for(int i=0;i<paramTags.length;i++){
			String comment = paramTags[i].parameterComment();
			if(!comment.isEmpty())	{	
				Counters.paramTagsAnalyzed++;
			}
			if(comment.contains("null")){
				Counters.paramMentionNull++;
			}
		}
		for(int i=0;i<method.throwsTags().length;i++){
			String comment = method.throwsTags()[i].exceptionComment();
			if(!comment.isEmpty())			
				Counters.throwTagsAnalyzed++;		
			if(comment.contains("null")){
				Counters.throwsMentionNull++;
			}
		}

		int ind=0;
		while(ind<params.length && ind<method.throwsTags().length)
		{
			String comment = method.throwsTags()[ind].exceptionComment();
			if(!isPrimitive(params[ind].typeName()) && comment.contains(params[ind].name()) && !comment.isEmpty()){
				Counters.throwTagsAnalyzedPri++;
					}
			if(!isPrimitive(params[ind].typeName()) && comment.contains(params[ind].name()) && comment.contains("null")){
				Counters.throwsNoPriNull++;
					}
			ind++;
		}
		
		for (Parameter param : params) {
			statusList.add(new NullStatus());
			boolean match = false;
			String paramName = param.name();

			for (ParamTag paramTag : paramTags) {
				// get the parameter name and comment for each @param tag
				String paramTagName = paramTag.parameterName();
				String comment = paramTag.parameterComment();

				if (comment.isEmpty() && statusInd < statusList.size()) {
					
					statusList.get(statusInd).setParamNull(paramName, 2);
				}
				
				
				// compare the parameter name in the method signature with
				// parameter name in the @param tag,ignoring the case difference
				if (paramName.compareToIgnoreCase(paramTagName) == 0) {
					// check if the parameter is previously mapped to tag
					if (!paramTagTable.containsKey(param)) {
						match = true;
						paramTagTable.put(param, paramTag);
						String combine = combineMethodNameAndParam(method,
								paramName);
						if (statusInd < statusList.size()) {
							int returnVal = analzeNullForParam(
									statusList.get(statusInd), combine, comment);
						}
					} else {
						System.out.println("DUPLICATE COMMENTS EXIST FOR "
								+ getMethodName(method) + ": " + paramName);
					}
				}
			}

			if (!match) {
				for (ParamTag tag : paramTags) {
					String combine = combineMethodNameAndParam(method,
							paramName);

					// check if the tag is previously matched
					if (statusInd < statusList.size()
							&& !paramTagTable.containsKey(param)
							&& !paramTagTable.containsValue(tag)) {
						matchTypeNameWithParam(method, params, paramTags,
								param, paramName, tag, combine,
								statusList.get(statusInd));
					}
				}

			}
			statusInd++;
		}
		return statusList;
	}

	private static void matchTypeNameWithParam(ExecutableMemberDoc method,
			Parameter[] params, ParamTag[] paramTags, Parameter param,
			String paramName, ParamTag tag, String combine, NullStatus status) {
		// check if the name of the parameter is the same as
		// the name of the type
		if (param.typeName().compareToIgnoreCase(tag.parameterName()) == 0) {
			paramTagTable.put(param, tag);
			analzeNullForParam(status, combine, tag.parameterComment());
		}
		// if the number of tags is the same as the number of
		// parameters, the remaining tag should be map to the remaining
		// parameter
		else if (params.length == paramTags.length) {
			analzeNullForParam(status, combine, tag.parameterComment());
		} 
		else {
			System.out.println(getMethodName(method) + ": " + paramName
					+ " - Parameter with this name do not exist");
		}
	}

	private static String getMethodName(ExecutableMemberDoc mems) {
		String methodName = mems.name();
		return methodName;
	}

	/**
	 * Analyze if the comment contains null and if a negation exists about null
	 * 
	 * @param comment
	 * @return 1 if the parameter can be null, 0 if the parameter cannot be null
	 *         or -1 if unknown
	 */
	public static int analyzeNull(String comment) {
		int nullLocation = comment.indexOf("null");
		int notIndex = comment.indexOf("not");
		int cannotIndex = comment.indexOf("cannot");
		int neverIndex = comment.indexOf("never");
		
		if(notIndex!=-1 && ((notIndex+3<comment.length() && Character.isLetter(comment.charAt(notIndex+3))) || (notIndex-1>0 && Character.isLetter(comment.charAt(notIndex-1)))))
				notIndex=-1;
		if(neverIndex!=-1 && ((neverIndex+5<comment.length() && Character.isLetter(comment.charAt(neverIndex+5))) || (neverIndex-1>0 && Character.isLetter(comment.charAt(neverIndex-1)))))
				neverIndex=-1;
		if(cannotIndex!=-1 && notIndex==-1)
				notIndex=cannotIndex;	
		// no negation exist
		if (nullLocation == -1 || comment.isEmpty()) {
			return -1;
		} 
		else if (comment.contains("nonnull") || comment.contains("non null")
				|| comment.contains("non-null")) {
			return 0;
		}
		// case where the parameter can be null since not negation exists
		else if (distanceOfNegation(comment, nullLocation, notIndex) == -1 && distanceOfNegation(
				comment, nullLocation, neverIndex) == -1) {
			return 1;
		}
		// case where the parameter can be null since the negation is too far away
		else if(distanceOfNegation(comment, nullLocation, neverIndex) > 3
				&& distanceOfNegation(comment, nullLocation, notIndex) > 3){
			return 1;
		}
		// case where the parameter cannot be null due to the negation
		else if (distanceOfNegation(comment, nullLocation, notIndex) <= 3
				|| distanceOfNegation(comment, nullLocation, neverIndex) <= 3) {
			return 0;

		}

		return -1;
	}

	public static int analyzeNull(String paramName, String comment) {
		int nullLocation = comment.indexOf("null");
		int paramLocation = comment.indexOf(paramName);
		int parameter = comment.indexOf("parameter");
		if (nullLocation != -1 && (paramLocation != -1 || parameter != -1)) {
			return findWordBeforeAndAfter(comment, paramName, nullLocation);
		} else
			return -1;
	}

	private static int findWordBeforeAndAfter(String comment, String paramName,
			int nullLocation) {
		String commentLowerCase = comment.toLowerCase();
		String[] strArrays = commentLowerCase.split(" ");
		boolean match = false;
		for (int index = 0; index < strArrays.length; index++) {
			if (strArrays[index].contains("null")) {
				// matches either pattern likes "null param" or "param is null"
				// or
				// "param can be null" or "param1 or param2 is null" or "param
				// is not valid
				// or null" or "either parameter is null"
				if ((index + 1 < strArrays.length && strArrays[index + 1]
						.equalsIgnoreCase(paramName))
						|| (index - 2 > 0 && strArrays[index - 2]
								.equalsIgnoreCase(paramName))
						|| (index - 3 > 0 && strArrays[index - 3]
								.equalsIgnoreCase(paramName))
						|| (index - 4 > 0
								&& strArrays[index - 4]
										.equalsIgnoreCase(paramName) && strArrays[index - 3]
								.equalsIgnoreCase("or"))
						|| (index - 5 > 0
								&& strArrays[index - 5]
										.equalsIgnoreCase(paramName) && strArrays[index - 1]
								.equalsIgnoreCase("or"))
						|| (index - 3 > 0 && strArrays[index - 2]
								.equalsIgnoreCase("parameter"))) {
					match = true;
					return 1;
				}

			} else if (strArrays[index].equalsIgnoreCase("nonnull")
					|| strArrays[index].equalsIgnoreCase("non-null")) {
				// matches either pattern likes nonnull/non-null param or param
				// is nonnull/non-null
				if (index + 1 < strArrays.length
						&& strArrays[index + 1].equalsIgnoreCase(paramName)
						|| (index - 2 > 0 && strArrays[index - 2]
								.equalsIgnoreCase(paramName))
						|| (index - 3 > 0 && strArrays[index - 3]
								.equalsIgnoreCase(paramName))) {
					match = true;
					return 0;
				}
			}
		}
		if (!match) {
			return -1;
		}
		return -1;
	}

	private static int distanceOfNegation(String comment, int nullIndex,
			int negationIndex) {
		int distance = 0;

		if (negationIndex == -1 || nullIndex == -1) {
			return -1;
		}
		// the negation appears before the null index
		if (negationIndex < nullIndex) {
			String wordInBetween = comment.substring(negationIndex, nullIndex);
			distance = wordcount(wordInBetween.trim());
		}
		// the negation appears after the null index
		else if (negationIndex > nullIndex) {
			String wordInBetween = comment.substring(nullIndex, negationIndex);
			distance = wordcount(wordInBetween.trim());
		}
		return distance;
	}

	private static int wordcount(String line) {
		int numWords = 0;
		int index = 0;
		boolean prevWhiteSpace = true;
		while (index < line.length()) {
			char c = line.charAt(index++);
			boolean currWhiteSpace = Character.isWhitespace(c);
			if (prevWhiteSpace && !currWhiteSpace) {
				numWords++;
			}
			prevWhiteSpace = currWhiteSpace;
		}

		return numWords;
	}



	private static int analzeNullForParam(NullStatus status, String name,
			String comment) {
		String commentLowerCase = comment.toLowerCase();
		int nullLocation = commentLowerCase.indexOf("null");
		int nullOrNot = -1;
		boolean invalidCharBeforeNull = false;
		if (nullLocation - 1 >= 0) {
			char charBeforeNull = commentLowerCase.charAt(nullLocation - 1);
			invalidCharBeforeNull = Character.isLowerCase(charBeforeNull)
					|| Character.isUpperCase(charBeforeNull);
		}
		if (commentLowerCase.startsWith("null")
				|| (nullLocation != -1 && !invalidCharBeforeNull)) {

			nullOrNot = analyzeNull(commentLowerCase);
		}
		if (nullOrNot == 1) {
			// set the param status to 1 indicating it's yes null
			status.setParamNull(name, 1);
			//counter.incrementParamTagYesNull();
		} else if (nullOrNot == 0) {
			// set the param status to 0 indicating it's not null
			status.setParamNull(name, 0);
			//counter.incrementParamTagNonNull();
		} else if (commentLowerCase.length() != 0) {
			// set the param status to -1 indicating it's unknown
			status.setParamNull(name, -1);
			//counter.incrementParamTagNoNull();
			// System.out.println("PARAM COMMENT:" + name +
			// " - Comments do not contain NULL");
		}
		return nullOrNot;
	}


	private static void printImplication(NullStatus status,
			String exceptionName, String paramName, int analyzeResult) {

		if (analyzeResult == 1) {
			String throwMsg = paramName + "==null =>" + exceptionName;
			status.setThrowIfNull(1, throwMsg);
		} else if (analyzeResult == 0) {
			String throwMsg = paramName + "!=null =>" + exceptionName;
			exceptionName = new String(exceptionName);
			status.setThrowIfNull(0, throwMsg);
		} else { 
			String throwMsg = exceptionName;
			if(!status.getExpectedException().contains(exceptionName))
				status.setExpectedException(exceptionName);
			if(exceptionName.contains(":null(")){
				throwMsg = paramName + "==null =>" + exceptionName;
				status.setThrowIfNull(1, throwMsg);
			}
			//status.setThrowIfNull(-1, null);
		}
	}

	private static void countExceptionType(String exceptionName) {

		if (exceptionName.equalsIgnoreCase("IllegalArgumentException")) {
			illegalArg++;
		}

		else if (exceptionName.equalsIgnoreCase("NullPointerException")) {
			nullPointer++;
		} 
		
		else {
			otherExcep++;
		}
	}

	private static String combineMethodNameAndParam(ExecutableMemberDoc mems,
			String paramName) {
		return (getMethodName(mems) + paramName);
	}
}
