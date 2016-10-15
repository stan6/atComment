package javadocAnalyzer;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.sun.javadoc.Parameter;

public class SentenceParser {

	public static List<String> analyzeOr(Parameter[] params,
			String str, String methodName) {
		String[] wordArrays = str.split(" ");
		List<String> subjArr = new LinkedList<String>();
		for (int index = 0; index < wordArrays.length; index++) {
			// look for the keyword "or"
			if (wordArrays[index].equalsIgnoreCase("or") || wordArrays[index].equalsIgnoreCase("either")){
				int sizeBefore2 = subjArr.size();
				subjArr=checkParamName(params, wordArrays, subjArr, index);
				int difference=subjArr.size() - sizeBefore2;
				return subjArr;
			}
		}

		return subjArr;
	}

	private static List<String> checkParamName(Parameter[] params, String[] wordArrays,
			List<String> subList, int index) {
		List<String> subjList=new LinkedList<String>();
		if (index > 0 && index < wordArrays.length) {

			for (Parameter param : params) {
				//extract the simple type name for each parameter
				//for example, java.util.Collection becomes Collection
				int dot=param.typeName().lastIndexOf(".");
				String typeName=param.typeName();
				//System.out.println("typeName:"+typeName);
				if(dot!=-1 && dot+1<typeName.length()){
					typeName=param.typeName().substring(dot+1);
					//System.out.println("after typeName:"+typeName);
				}
				// check if the phrase "parameter" or "argument" is mentioned
				if (wordArrays[index].equalsIgnoreCase("parameter")
						|| wordArrays[index].equalsIgnoreCase("argument")) {
					subjList.add(params[0].name());
					return subjList;
				}
				//if the phrase "either" is mentioned and the length of the parameter list is 2
				//treat both as the sub
				else if((wordArrays[index].equalsIgnoreCase("either") && params.length == 2)){
					subjList.add(params[0].name());
					subjList.add(params[1].name());
					return subjList;
				}
				//if the phrase "or" is mentioned and the length of the parameter list is 2
				//treat both as the sub
				else if((wordArrays[index].equalsIgnoreCase("or") && params.length == 2)){
					subjList.add(params[0].name());
					subjList.add(params[1].name());
					return subjList;
				}
				//the phrase "parameters" and "arguments" means all the parameters in the list
				else if(wordArrays[index].equalsIgnoreCase("parameters")
						|| wordArrays[index].equalsIgnoreCase("arguments")){
					for(Parameter par:params){
						subjList.add(param.name());
					}
					return subjList;
				}
				// check if the param name is mentioned
				if (param.name().equalsIgnoreCase(wordArrays[index])) {
					subjList.add(param.name());
					return subjList;
				}
				// check if the type of the parameter is mentioned
				else if (typeName.equalsIgnoreCase(wordArrays[index])) {
					subjList.add(wordArrays[index]);
					return subjList;
				} else if ((index + 1 < wordArrays.length && param.name()
						.equalsIgnoreCase(
								wordArrays[index] + wordArrays[index + 1]))) {
					subjList.add(wordArrays[index]+" "+ wordArrays[index + 1]);
					return subjList;
				}
			}
		}
		return subjList;
	}

	/*
	 * Get the value of the subject at index. Returns null if the value cannot
	 * be predicted.
	 */
	private static List<String> getValue(String subject, String[] wordArrays,
			int index) {
		List<String> valueList = new LinkedList<String>();

		int isIndex = index + 1;
		if (isIndex < wordArrays.length
				&& (wordArrays[isIndex].equalsIgnoreCase("is") || wordArrays[isIndex]
						.equalsIgnoreCase("are"))) {

			if (wordArrays[isIndex + 1].contains("null")) {
				valueList.add("null");
			}
		} else if (isIndex + 2 < wordArrays.length
				&& (wordArrays[isIndex + 1].equalsIgnoreCase("is") || wordArrays[isIndex + 1]
						.equalsIgnoreCase("are"))) {
			valueList.add(wordArrays[isIndex + 2]);
		}
		if (isIndex + 3 < wordArrays.length
				&& wordArrays[isIndex].equalsIgnoreCase("or")
				&& (wordArrays[isIndex + 2].equalsIgnoreCase("is") || wordArrays[isIndex + 2]
						.equalsIgnoreCase("are"))) {
			valueList.add(wordArrays[isIndex + 3]);
		}

		if (isIndex + 4 < wordArrays.length
				&& wordArrays[isIndex + 2].equalsIgnoreCase("or")
				&& wordArrays[isIndex + 3].equalsIgnoreCase("not")) {
			valueList.add(wordArrays[isIndex + 3] + " "
					+ wordArrays[isIndex + 4]);
		}
		return valueList;
	}

	public static List<String> analyzeIf(Parameter[] params,
			String str, String methodName) {
		// List<String> implicationlist=new LinkedList<String>();
		List<String> subjectAndVal = new LinkedList<String>();
		String[] wordArrays = str.split(" ");
		for (int index = 0; index < wordArrays.length; index++) {
			if (wordArrays[index].equalsIgnoreCase("if")
					|| wordArrays[index].equalsIgnoreCase("when")) {
				subjectAndVal = analyzeOr(params, str, methodName);
			}
		}
		if(subjectAndVal.size()==0){
			for (int index = 0; index < wordArrays.length; index++) {
				List<String> temp=checkParamName(params, wordArrays, subjectAndVal, index);
				subjectAndVal.addAll(temp);
			}
		}
		if(methodName.contains("loopingIterator")){
			for(String string:subjectAndVal){
				System.out.println("sub:"+string);
			}
		}
		return subjectAndVal;
	}
}
