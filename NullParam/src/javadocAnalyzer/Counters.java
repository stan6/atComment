package javadocAnalyzer;

public class Counters {
	private int noMethodComment;
	private int methodCommentYesNull;
	private int methodCommentNonNull;
	private int methodCommentNoNull;
	private int noParamComment;
	private int paramTagYesNull;
	private int paramTagNonNull;
	private int paramTagNoNull;
	private int noThrowComment;
	private int throwTagYesNull;
	private int throwTagNonNull;
	private int throwTagNoNull;
	private int noReturnComment;
	private int retTagYesNull;
	private int retTagNonNull;
	private int retTagNoNull;
	private int param0throw1;
	private int param0throw0;
	private int param0throwNeg;
	private int paramNegthrow1;
	private int param1throw1;
	public static int paramTagsAnalyzed=0;
	public static int throwTagsAnalyzed=0;
	public static int paramTagsAnalyzedPri=0;
	public static int throwTagsAnalyzedPri=0;
	public static int yesNullThrow=0;
	public static int nonNull=0;
	public static int yesNull=0;
	public static int unknownNull=0;
        public static int paramMentionNull=0;
	public static int throwsMentionNull=0;
        public static int methodMentionNull=0;
	public static int methodBlock=0;
	public static int primitive=0;
	public static int paramTypeNull=0;
	public static int paramThrowsNull=0;
	public static int throwsNoPriNull=0;
	public static int paramNoPriNull=0;
	public static int totMethods=0;
	public static double totMethodsCovered;
	public static double totMethodsNonPriCovered;

	Counters(){
		noMethodComment=0;
		methodCommentYesNull=0;
		methodCommentNonNull=0;
		methodCommentNoNull=0;
		noParamComment=0;
		paramTagYesNull=0;
		paramTagNonNull=0;
		paramTagNoNull=0;
		noThrowComment=0;
		throwTagYesNull=0;
		throwTagNonNull=0;
		throwTagNoNull=0;
		noReturnComment=0;
		param0throw1=0;
		param0throw0=0;
		param0throwNeg=0;
		paramNegthrow1=0;
		param1throw1=0;
	}
	
	void incrementNoMethodComment() {
		this.noMethodComment = getNoMethodComment() + 1;
	}

	int getNoMethodComment() {
		return noMethodComment;
	}
	
	void incrementMethodCommentYesNull() {
		this.methodCommentYesNull = getMethodCommentYesNull() + 1;
	}
	
	int getMethodCommentYesNull() {
		return methodCommentYesNull;
	}
	
	void incrementParamTagYesNull() {
		this.paramTagYesNull = getParamTagYesNull() + 1;
	}
	
	int getParamTagYesNull() {
		return paramTagYesNull;
	}
	
	void incrementParamTagNonNull() {
		this.paramTagNonNull = getParamTagNonNull() + 1;
	}
	
	int getParamTagNonNull() {
		return paramTagNonNull;
	}
	
	void incrementParamTagNoNull() {
		this.paramTagNoNull = getParamTagNoNull() + 1;
	}
	
	int getParamTagNoNull() {
		return paramTagNoNull;
	}
	
	void incrementMethodCommentNonNull() {
		this.methodCommentNonNull = getMethodCommentNonNull() + 1;
	}
	
	int getMethodCommentNonNull() {
		return methodCommentNonNull;
	}
	
	void incrementMethodCommentNoNull() {
		this.methodCommentNoNull = getMethodCommentNoNull() + 1;
	}
	
	int getMethodCommentNoNull() {
		return methodCommentNoNull;
	}
	
	void incrementNoParamComment() {
		this.noParamComment = getNoParamComment() + 1;
	}
	
	int getNoParamComment() {
		return noParamComment;
	}
	
	void incrementNoThrowComment() {
		this.noThrowComment = getNoThrowComment() + 1;
	}
	
	int getNoThrowComment() {
		return noThrowComment;
	}
	
	void incrementThrowTagYesNull() {
		this.throwTagYesNull = getThrowTagYesNull() + 1;
	}
	
	int getThrowTagYesNull() {
		return throwTagYesNull;
	}
	
	void incrementThrowTagNonNull() {
		this.throwTagNonNull = getThrowTagNonNull() + 1;
	}
	
	int getThrowTagNonNull() {
		return throwTagNonNull;
	}
	
	void incrementThrowTagNoNull() {
		this.throwTagNoNull = getThrowTagNoNull() + 1;
	}
	
	int getThrowTagNoNull() {
		return throwTagNoNull;
	}
	
	void incrementNoReturnComment() {
		this.noReturnComment = getNoReturnComment() + 1;
		
	}

	int getNoReturnComment() {
		return noReturnComment;
	}
	
	void incrementRetTagYesNull() {
		this.retTagYesNull = getRetTagYesNull() + 1;
	}
	
	int getRetTagYesNull() {
		return retTagYesNull;
	}
	
	void incrementRetTagNonNull() {
		this.retTagNonNull = getRetTagNonNull() + 1;
	}
	
	int getRetTagNonNull() {
		return retTagNonNull;
	}
	
	void incrementRetTagNoNull() {
		this.retTagNoNull = getRetTagNoNull() + 1;
	}
	
	int getRetTagNoNull() {
		return retTagNoNull;
	}
	
	void incrementParam0throw1() {
		this.param0throw1 = getParam0throw1() + 1;
	}

	int getParam0throw1() {
		return param0throw1;
	}
	void incrementParam0throw0() {
		this.param0throw0 = getParam0throw0() + 1;
	}

	int getParam0throw0() {
		return param0throw0;
	}
	
	void incrementParam0throwNeg() {
		this.param0throwNeg = getParam0throwNeg() + 1;
	}

	int getParam0throwNeg() {
		return param0throwNeg;
	}
	
	void incrementParamNegthrow1() {
		this.paramNegthrow1 = getParamNegthrow1() + 1;
	}

	int getParamNegthrow1() {
		return paramNegthrow1;
	}
	
	void incrementParam1throw1() {
		this.param1throw1 = getParam1throw1() + 1;
	}

	int getParam1throw1() {
		return param1throw1;
	}
	
	public static void printAll(){
		System.out.println("Param tags:"+paramTagsAnalyzed);
		System.out.println("Throw tags:"+throwTagsAnalyzed);
		System.out.println("Param tags Pri:"+paramTagsAnalyzedPri);
		System.out.println("Throw tags Pri:"+throwTagsAnalyzedPri);
		System.out.println("YesNull exception:"+yesNullThrow);
		System.out.println("YesNull only:"+yesNull);
		System.out.println("NonNull only:"+nonNull);
		System.out.println("UnknownNull only "+unknownNull);
		System.out.println("Param mentioned null:"+paramMentionNull);
		System.out.println("Param mentioned null that is nonprimitive:"+paramNoPriNull);
		System.out.println("Throws mentioned null:"+throwsMentionNull);
		System.out.println("Throws mentioned null that is nonprimitive:"+throwsNoPriNull);
		System.out.println("Method mentioned null:"+methodMentionNull);
		System.out.println("Total method block:"+methodBlock);
		System.out.println("Total methods:"+totMethods);
		System.out.println("Total percentage:"+totMethodsCovered);
		System.out.println("Total percentage/totMethods:"+(totMethodsCovered/totMethods));
		System.out.println("Total percentage non-primitive:"+totMethodsNonPriCovered);
		System.out.println("Total percentage non-primitive:/totMethods:"+(totMethodsNonPriCovered/totMethods));


	}

}

