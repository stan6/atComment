package randoop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;

import daikon.util.FileIOException;
import randoop.main.GenInputsAbstract;
import randoop.util.NullStatus;

public class InferNullUtils {

  public static int countSkip = 0;
  public static int countNullPointer = 0;
  public static int countIllegalArg = 0;
  private LinkedList<NullStatus> nullList;
  /** File for logging inference info for ALL statements ever executed */
  private static File logAllFile = null;
  private static BufferedWriter logAllWriter = null;
  /** File for logging inference info for ONLY statements appearing as the last statement in a test sequence */
  private static File logTestFile = null;
  private static BufferedWriter logTestWriter = null;

  /** Used to associate null-status inferences with input parameters **/
  private static HashMap<String, HashMap<Integer, NullStatus>> nullStatusMap = new HashMap<String, HashMap<Integer, NullStatus>>();

  /**
   * 
   * @return a list containing all the information about nullability or return
   *         null if the GenInputsAbstract.sourcepath is null
   * 
   */
  public List<NullStatus> getNullList() {
    if (this.nullList == null) {
      String inferFolderPath = GenInputsAbstract.sourcepath;

      if (GenInputsAbstract.sourcepath == null) {
        return null;
      }
      // create the infer directory that will contain all the files
      File dir = new File(inferFolderPath);
      if (dir.exists() && dir.isDirectory()) {
        // get the path for the src by finding the word src
        String srcPath = GenInputsAbstract.sourcepath;
        int beforeslash = GenInputsAbstract.sourcepath.lastIndexOf("src");
        if (beforeslash == -1)
          beforeslash = GenInputsAbstract.sourcepath.lastIndexOf("source");
        if (beforeslash != -1)
          srcPath = GenInputsAbstract.sourcepath.substring(0, beforeslash);
        if (srcPath.endsWith(System.getProperty("file.separator"))) {

          inferFolderPath = srcPath + "inferNull";
        } else {
          inferFolderPath = srcPath + System.getProperty("file.separator") + "inferNull";
        }

        // check if the infer directory is created
        dir = new File(inferFolderPath);
        if (!dir.exists() || !dir.isDirectory()) {
          System.err.println("ERROR: The infer directory created is not found at path " + inferFolderPath);
          System.exit(1);
        }
      } else {
        System.err.println("ERROR: Invalid sourcepath at " + inferFolderPath);
        System.exit(1);
      }

      if (inferFolderPath.contains("inferNull")) {
        parseJMLAndInfer(dir, ".infer");
        // parseJMLAndInfer(dir, ".jml");
      }
    }
    return this.nullList;
  }

  /**
   * Filter and parse all the infer and jml files to obtain the null-related
   * information.
   * 
   * @param dir
   *          to search for the infer or jml files
   * @param extension
   *          that is either ".infer" or ".jml"
   * @return
   */
  private void parseJMLAndInfer(File dir, final String extension) {
    // look for all the infer or jml files
    FilenameFilter filefilter = new FilenameFilter() {

      public boolean accept(File dir, String name) {
        // if the file extension is .infer or .jml return true, else
        // false
        return name.endsWith(extension);
      }
    };

    // read and parse all the files with the ".infer" or ".jml" extension
    File[] files = dir.listFiles(filefilter);
    this.nullList = new LinkedList<NullStatus>();

    if (extension.contains(".infer")) {
      for (File file : files) {
        if (file.canRead()) {
          List<NullStatus> retList = getNullInfoFromInfer(file);
          this.nullList.addAll(retList);
        }
      }
    } else if (extension.contains(".jml")) {
      for (File file : files) {
        if (file.canRead()) {
          List<NullStatus> retList = parseNullInfoFromJML(file);
          this.nullList.addAll(retList);
        }
      }
    }
  }

  public List<NullStatus> getNullInfoFromInfer(File file) {
    List<NullStatus> nullList = new LinkedList<NullStatus>();
    try {
      BufferedReader in = parseNullInfoFromInfer(file, nullList);
      in.close();
    } catch (IOException e) {
      System.err.println("ERROR WHILE READ .infer FILES");
      System.exit(-1);
    }

    addNullStatusesToMap(nullList);

    return nullList;
  }

  private BufferedReader parseNullInfoFromInfer(File file, List<NullStatus> nullList) throws FileNotFoundException, IOException {
    BufferedReader in = new BufferedReader(new FileReader(file.getCanonicalPath()));

    String str = "";
    String paramName = "";
    int extension = file.getName().indexOf(".infer");
    String fileName = file.getName().substring(0, extension);
    List<String> exceptionlist = new LinkedList<String>();
    while ((str = in.readLine()) != null) {

      // case where the parameter can be null
      if (str.startsWith("@YesNull")) {
        NullStatus status = new NullStatus();
        // case where no exception is specified
        if (!str.contains("=>")) {
          paramName = stringInBetween(str, "(", ")");
          if (paramName != null) {
            status.setParamNull(paramName, 1);
            nullList.add(status);
          }
        }
        // case with exception
        else {
          int beforeExcep = str.indexOf(">");
          str.substring(beforeExcep + 1);
          int nullIndex = str.indexOf("null");
          if (nullIndex - 3 >= 0) {
            paramName = stringInBetween(str, "(", "==null");
            status.setParamNull(paramName, 1);
            String exceptionName = stringInBetween(str, ">", ")");
            if (str.indexOf("!") != -1) {
              status.setThrowIfNull(0, exceptionName);
            } else {
              status.setThrowIfNull(1, exceptionName);
            }
            nullList.add(status);
            // add :null( for identifying the location of the exception
            // related to null
            exceptionlist.add(":null(" + exceptionName);
          }
        }
      } else if (str.startsWith("@NonNull")) {
        NullStatus status = new NullStatus();
        paramName = stringInBetween(str, "(", ")");
        if (paramName != null) {
          status.setParamNull(paramName, 0);
          nullList.add(status);
        }
      }
      /*
       * Case added for OtherException
       */
      else if (str.startsWith("@OtherException")) {
        String exceptionName = stringInBetween(str, "(", ")");
        exceptionlist.add(exceptionName);
      } else if (str.startsWith("@Unknown") || str.startsWith("@UnknownNull")) {
        NullStatus status = new NullStatus();
        paramName = stringInBetween(str, "(", ")");
        if (paramName != null) {
          status.setParamNull(paramName, -1);
          nullList.add(status);
        }
      } else if (!str.startsWith("@") && !str.isEmpty()) {

        for (NullStatus status : nullList) {
          if (status.getMethodSignature() == null) {
            status.setMethodName(getMethodName(str));
            status.setMethodSignature(str);
            status.setClassName(fileName);
            // if(status.getOtherexceptionNames()!=null)
            status.setTypeNameAndPos();
            if (exceptionlist.size() > 0) {
              for (String except : exceptionlist)
                status.setOtherexceptionNames(except);
            }
          }
        }
        exceptionlist = new LinkedList<String>();

      }

    }
    return in;
  }

  public List<NullStatus> parseNullInfoFromJML(File file) {
    List<NullStatus> nullList = new LinkedList<NullStatus>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(file.getCanonicalPath()));
      String str = "";
      List<String> paramNames = new LinkedList<String>();
      int extension = file.getName().indexOf(".jml");
      String fileName = file.getName().substring(0, extension);
      while ((str = in.readLine()) != null) {

        String require = "@   requires ";
        String signal = "@   signals_only ";
        // parse the parameter names
        if (str.startsWith(require)) {
          // the length of the require string should be 13
          int afterRequires = require.length();
          int endName = str.indexOf("=");
          int or = str.indexOf("||");

          // case without any || symbol
          if (endName >= 1 && or == -1) {
            paramNames.add(str.substring(afterRequires, endName));
          }
          // case with || symbol
          else {
            int semicolon = str.indexOf(";");
            String startParams = str.substring(afterRequires, semicolon);
            String[] names = startParams.split("==null( \\|\\|)*");
            for (String name : names) {
              if (!name.contains("||")) {
                paramNames.add(name);
              } else {
                paramNames.add(name);
              }
            }
          }

        }
        // parse the exception name
        else if (str.startsWith(signal) && !paramNames.isEmpty()) {
          int afterSignal = signal.length();
          int endExcep = str.indexOf(";");
          String exceptionName = str.substring(afterSignal, endExcep);
          for (String paramName : paramNames) {
            if (paramName != null) {
              NullStatus status = new NullStatus();
              status.setParamNull(paramName, 1);
              status.setThrowIfNull(1, exceptionName);
              nullList.add(status);
            }
          }
          // clear the list so that other method can use the same list
          paramNames.clear();
        }

        else if (!str.isEmpty()) {
          int startParamList = str.indexOf("(");
          int endParamList = str.indexOf(")");

          if (startParamList != -1 && endParamList != -1) {
            String paramList = str.substring(startParamList + 1, endParamList);
            paramList = paramList.trim();
            String[] params = paramList.split(",");
            int loc = 0;
            for (String param : params) {

              // remove space before and after comma
              param = param.trim();
              int beforeName = param.indexOf(" ");

              String methodName = getMethodName(str);
              String sig = str.replaceAll("/\\*@nullable\\*/", "");
              sig = sig.replaceAll("/\\*@non_null\\*/", "");
              NullStatus status = new NullStatus(methodName, sig, fileName, params.length);

              if (beforeName != -1) {
                String typeName = param.substring(0, beforeName);
                if (!typeName.isEmpty()) {
                  status.setTypeName(typeName, loc);
                }
              }
              // case where /*@nullable*/ is specified
              String paramName = param.substring(beforeName + 1);
              if (beforeName != -1 && param.contains("/*@nullable*/")) {
                status.setParamNull(paramName, 1);
              }
              // case where /*@non_null*/ is specified
              else if (beforeName != -1 && param.contains("/*@non_null*/")) {
                status.setParamNull(paramName, 0);
              }
              // case for unknown null
              else if (beforeName != -1) {
                status.setParamNull(paramName, -1);
              }

              nullList.add(status);
              loc++;
            }
          }
        }
      }
      in.close();
    } catch (IOException e) {
      System.err.println("ERROR WHILE READ .jml FILES");
      System.exit(-1);
    }
    return nullList;
  }

  public String getMethodName(String methodSig) {
    String[] methodName = methodSig.split("\\s*\\(");
    String[] firstHalf = methodName[0].split("\\s+");
    return firstHalf[firstHalf.length - 1];
  }

  private static String[] getParamTypesForSignature(String methodSig) {
    String[] methodName = methodSig.split("\\s*\\(");
    if (methodName.length > 1) {
      String secondHalf = methodName[1].substring(0, methodName[1].indexOf(")"));
      String[] types = secondHalf.split(",");

      for (int i = 0; i < types.length; i++) {
        if (types[i].trim().contains(" "))
          types[i] = types[i].substring(0, types[i].trim().indexOf(" "));
      }

      return types;
    }
    return methodName;
  }

  public static boolean matchParamList(String methodSig, List<String> paramList) {
    String[] types = getParamTypesForSignature(methodSig);

    if (types.length != paramList.size()) {
      return false;
    } else {
      for (int i = 0; i < paramList.size(); i++) {
        if (!types[i].trim().equalsIgnoreCase(paramList.get(i))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Given the original string, this method will return the string between the
   * begin string and the end string
   * 
   * @param original
   * @param begin
   * @param end
   * @return the string between begin and end without trailing or leading space
   */
  public String stringInBetween(String original, String begin, String end) {

    int startIndx = original.indexOf(begin);
    int endIndx = original.indexOf(end);

    if (startIndx != -1 && endIndx != -1) {
      String strBetween = original.substring(startIndx + 1, endIndx);
      return (strBetween.trim());
    }
    return null;
  }

  public NullStatus matchStatementWithNull(ExecutableSequence seq, int idx, LinkedList<NullStatus> nullList) {
    NullStatus returnStat = new NullStatus();
    returnStat.exceptionMap = new HashMap<String, String>();
    List<Integer> nullLocs = checkLocSeqContainsNull(seq, idx);
    if (!nullLocs.isEmpty()) {
      String methodName = null;
      Class<?>[] classes = null;
      String rclassName = null;
      if (seq.sequence.getStatementKind(idx) instanceof RMethod) {
        RMethod rmethod = (RMethod) seq.sequence.getStatementKind(idx);
        Method method = rmethod.getMethod();
        methodName = method.getName();
        rclassName = method.getDeclaringClass().getSimpleName();
        classes = rmethod.getMethod().getParameterTypes();
      }

      else if (seq.sequence.getStatementKind(idx) instanceof RConstructor) {
        RConstructor rcons = (RConstructor) seq.sequence.getStatementKind(idx);
        methodName = rcons.getConstructor().getName();
        classes = rcons.getConstructor().getParameterTypes();
        // the name of a constructor is the same as the name of a class
        rclassName = methodName;
      }
      returnStat.className = rclassName;
      List<String> paramList = new LinkedList<String>();
      paramList.clear();
      for (Class<?> clss : classes) {
        paramList.add(clss.getSimpleName());
      }

      for (NullStatus status : nullList) {
        String className = status.getClassName();

        if (rclassName.equals(className) && status.getMethodName() != null && !methodName.isEmpty() && methodName.equals(status.getMethodName())) {

          // match null loc
          if (!nullLocs.contains(status.getLoc()))
            continue;

          if (status.getParamNull() == 2 || status.getParamNull() == -1)
            continue;

          if (!matchParamList(status.getMethodSignature(), paramList)) {
            continue;
          }

          if (status.getOtherexceptionNames().size() > 0 && status.getMethodSignature() != null) {
            String nullRelated = ":null(";

            for (String exceptionName : status.getOtherexceptionNames()) {
              if (returnStat.getMethodSignature() == null)
                returnStat.setMethodSignature(status.getMethodSignature());
              if (!exceptionName.startsWith(nullRelated) && !returnStat.exceptionMap.containsKey(exceptionName))
                returnStat.exceptionMap.put(exceptionName, "OtherException");
            }

            List<String> excepts = status.getOtherexceptionNames();
            for (String excep : excepts) {
              returnStat.setOtherexceptionNames(excep);
            }

          }

          if (status.getTypeName() != null && paramList.contains(status.getTypeName()) && status.getLoc() >= 0 && status.getLoc() < paramList.size()
              && paramList.get(status.getLoc()).contains(status.getTypeName())) {

            if ((status.getParamNull() == 1 || status.getThrowIfNull() == 1) && status.getThrowImplication() != null) {
              if (returnStat.getMethodSignature() == null)
                returnStat.setMethodSignature(status.getMethodSignature());

              String exceptionName = status.getThrowImplication();
              String annoType = "NullSpecific";
              if (!returnStat.exceptionMap.containsKey(exceptionName))
                returnStat.exceptionMap.put(exceptionName, annoType);
            }

            // case for @Nonnull
            else if (status.getParamNull() == 0 || status.getThrowIfNull() == 1) {
              if (returnStat.getMethodSignature() == null)
                returnStat.setMethodSignature(status.getMethodSignature());
              String annoType = "NonNull";
              if (!returnStat.exceptionMap.containsKey("Exception"))
                returnStat.exceptionMap.put("Exception", annoType);
            }

            // case for @Yesnull
            else if (status.getParamNull() == 1) {
              if (returnStat.getMethodSignature() == null)
                returnStat.setMethodSignature(status.getMethodSignature());
              String annoType = "YesNull";
              if (!returnStat.exceptionMap.containsKey("Normal"))
                returnStat.exceptionMap.put("Normal", annoType);
            }

            // case for @Unknownnull
            /*
             * else if (status.getParamNull() == -1 || status.getParamNull() ==
             * 2) { if(returnStat.getMethodSignature()==null)
             * returnStat.setMethodSignature(status.getMethodSignature());
             * String annoType="Unknown";
             * if(!returnStat.exceptionMap.containsKey("Unknown"))
             * returnStat.exceptionMap.put("Unknown", annoType); }
             */}
        }
      }
    }

    return returnStat;
  }

  public List<Integer> checkLocSeqContainsNull(ExecutableSequence s, int idx) {
    List<Variable> variables = s.sequence.getInputs(idx);
    int loc = 0;
    List<Integer> locs = new LinkedList<Integer>();
    StatementKind st = s.sequence.getStatementKind(idx);
    // ignore first variable for static method
    if (st instanceof RMethod && (!((RMethod) st).isStatic())) {
      loc = -1;
    }

    Object[] runtimeVals = ExecutableSequence.getRuntimeValuesForVars(variables, Arrays.asList(s.getAllResults()));
    for (Object runtimeVal : runtimeVals) {
      if (runtimeVal == null)
        locs.add(loc);
      loc++;
    }
    return locs;
  }
 
  public List<Integer> checkVarNamesContainsNull(ExecutableSequence s, int idx) {
    List<Variable> variables = s.sequence.getInputs(idx);
    int loc = 0;
    List<Integer> locs = new LinkedList<Integer>();
    StatementKind st = s.sequence.getStatementKind(idx);
    // ignore first variable for static method
    if (st instanceof RMethod && (!((RMethod) st).isStatic())) {
      loc = -1;
    }

    Object[] runtimeVals = ExecutableSequence.getRuntimeValuesForVars(variables, Arrays.asList(s.getAllResults()));
    for (Object runtimeVal : runtimeVals) {
      if (runtimeVal == null)
        locs.add(loc);
      loc++;
    }
    return locs;
  }
  
  public boolean checkReceiverContainsNull(ExecutableSequence s, int idx) {
    List<Variable> variables = s.sequence.getInputs(idx);
    StatementKind st = s.sequence.getStatementKind(idx);
    // ignore first variable for static method
    if (st instanceof RMethod && (!((RMethod) st).isStatic())) {
      // check if the receiver's declaration involves any null value
      int receiverDecl = variables.get(0).getDeclIndex();
      StatementKind declSt = s.sequence.getStatementKind(receiverDecl);
      if (declSt instanceof RConstructor) {
        List<Variable> consVars = s.sequence.getInputs(receiverDecl);
        Object[] runtimeConVals = ExecutableSequence.getRuntimeValuesForVars(consVars, Arrays.asList(s.getAllResults()));
        for (Object conVal : runtimeConVals) {
          if (conVal == null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /***
   * For each statement in seq that has inferences associated with it, this logs
   * the parameter inferences and their null status (whether they're
   * null/non-null/primitive) to a text file in the same directory as the
   * Randoop generated tests.
   * 
   * It also logs, in a different file, the same inference info about the last statement
   * in the sequence.
   * 
   * @param seq
   *          The Sequence whose statements you want to log inference and null
   *          status info for.
   */
  public static void logInferenceStatusInfo(Sequence seq) {
    try {
      setupLogFiles();
    } catch (IOException e) {
      System.out.println("Could not set up inference log file: " + e);
      e.printStackTrace();
      return;
    }

    try {
      String inferenceString;
      
      // Output inferences and null statuses for every statement in the sequence
      for (int i = 0; i < seq.getStatementsWithInputs().size(); i++) {
        StatementKind stmt = seq.getStatementsWithInputs().get(i).statement;
        List<Variable> vars = seq.getInputs(i);
        
        inferenceString = getInferenceString(stmt, vars);
        
        // If there is inference info, log it
        if (inferenceString != null) {
          // Log info for every one to logAllFile
          logAllWriter.write(inferenceString);
          logAllWriter.newLine();
          
          // Only log this info to logTestFile if it's the last statement
          if (stmt == seq.getLastStatement()) {
            logTestWriter.write(inferenceString);
            logTestWriter.newLine();
          }
        }
      }
    } catch (IOException e) {
      System.out.println("Error while logging inference info: " + e);
      e.printStackTrace();
    } finally {
      closeLogFiles();
    }
  }
    
  private static String getInferenceString(StatementKind stmt, List<Variable> vars) {    
    String inferenceString = null;
    HashMap<Integer, String> knownInferences = getKnownInferences(stmt);
    
    // If this statement has any associated inferences, we'll print the info
    // about it
    if (knownInferences.size() > 0) {
      List<Class<?>> inputTypes = stmt.getInputTypes();
      HashMap<Integer, NullStatus> nullStatuses = InferNullUtils.getNullStatusesForStatement(stmt);
      NullStatus firstStatus = nullStatuses.values().iterator().next();

      inferenceString = "Class " + firstStatus.getClassName() + "\tSig: " + firstStatus.getMethodSignature() + "\tStatuses: ";

      for (int j = 0; j < inputTypes.size(); j++) {
        Class<?> t = inputTypes.get(j);

        inferenceString += t.getSimpleName() + "/";

        String inference = knownInferences.get(j);
        
        if (inference == null) {
          inferenceString += "Unknown";
        } else {
          inferenceString += inference;
        }

        inferenceString += "/";

        if (t.isPrimitive()) {
          inferenceString += "PRIM ";
        } else if (vars.get(j).getDeclaringStatement() instanceof PrimitiveOrStringOrNullDecl) {
          inferenceString += "NULL ";
        } else {
          inferenceString += "NON ";
        }
        inferenceString += ",";
      }
    }
    
    return inferenceString;
  }

  /***
   * Attempts to create and open log file and directories.
   * 
   * @throws IOException
   */
  private static void setupLogFiles() throws IOException {
    File dir = new File(GenInputsAbstract.junit_output_dir);

    if (!dir.exists()) {
      dir.mkdir();
    }

    logAllFile = new File(GenInputsAbstract.junit_output_dir, "allStatementStatuses.txt");
    logTestFile = new File(GenInputsAbstract.junit_output_dir, "testStatementStatuses.txt");

    if (!logAllFile.exists()) {
      logAllFile.createNewFile();
    }

    if (!logTestFile.exists()) {
      logTestFile.createNewFile();
    }

    logAllWriter = new BufferedWriter(new FileWriter(logAllFile, true));
    logTestWriter = new BufferedWriter(new FileWriter(logTestFile, true));
  }

  private static void closeLogFiles() {
    try {
      logAllWriter.close();
    } catch (IOException e) {
      System.out.println("Error closing log file: " + e);
      e.printStackTrace();
    }
    try {
      logTestWriter.close();
    } catch (IOException e) {
      System.out.println("Error closing log file: " + e);
      e.printStackTrace();
    }
  }

  /**
   * Populates the static member nullStatusMap.
   * 
   * @param nullList
   *          List from which to populate nullStatusMap.
   */
  private static void addNullStatusesToMap(List<NullStatus> nullList) {
    for (NullStatus status : nullList) {
      String key = getNullStatusMapKey(status);

      if (!nullStatusMap.containsKey(key)) {
        nullStatusMap.put(key, new HashMap<Integer, NullStatus>());
      }

      nullStatusMap.get(key).put(status.getLoc(), status);
    }
  }

  public static String getNullStatusMapKey(StatementKind statement) {
    String methodName = null;
    String className = null;
    Class<?>[] classes = null;

    if (statement instanceof RMethod) {
      RMethod rmethod = (RMethod) statement;
      Method method = rmethod.getMethod();
      methodName = method.getName();
      className = method.getDeclaringClass().getSimpleName();
      classes = rmethod.getMethod().getParameterTypes();
    } else if (statement instanceof RConstructor) {
      RConstructor rcons = (RConstructor) statement;
      methodName = rcons.getConstructor().getDeclaringClass().getSimpleName();
      className = methodName;
      classes = rcons.getConstructor().getParameterTypes();
    // If it's not a method or constructor, we don't have inference info about
    // it,
    // so return null.
    } else {
      return null;
    }

    List<String> paramList = new ArrayList<String>();
    paramList.clear();
    for (Class<?> clss : classes) {
      paramList.add(clss.getSimpleName());
    }

    String[] types = new String[paramList.size()];
    for (int i = 0; i < types.length; i++)
      types[i] = paramList.get(i);

    return getNullStatusMapKey(className, methodName, types);
  }

  private static String getNullStatusMapKey(NullStatus status) {
    return getNullStatusMapKey(status.getClassName(), status.getMethodName(), getParamTypesForSignature(status.getMethodSignature()));
  }

  private static String getNullStatusMapKey(String className, String methodName, String[] types) {
    String key = className + " " + methodName + " ";

    for (String type : types) {
      key += type.trim() + " ";
    }

    return key;
  }

  /**
   * @param statement
   * @return A HashMap of index/loc to NullStatuses for all inferences
   *         associated with the statement. Empty if none.
   */
  public static HashMap<Integer, NullStatus> getNullStatusesForStatement(StatementKind statement) {
    HashMap<Integer, NullStatus> statuses = nullStatusMap.get(getNullStatusMapKey(statement));

    if (statuses == null) {
      statuses = new HashMap<Integer, NullStatus>();
    }

    return statuses;
  }
  
  /**
   * Returns a mapping from index of parameter to a string describing the known null-related inference
   * for that parameter, in a given statement.
   * @param statement
   * @return
   */
  public static HashMap<Integer, String> getKnownInferences(StatementKind statement) {  
    HashMap<Integer, String> inferences = new HashMap<Integer, String>();
    
    HashMap<Integer, NullStatus> statuses = getNullStatusesForStatement(statement);
       
    for (Integer i : statuses.keySet()) {
      NullStatus status = statuses.get(i);
      // If status is null, this param is Unknown
      if (status == null || status.getParamNull() == -1 || status.getParamNull() == 2) {
        // Unknown
        continue;
      } else {
        // Normal
        if (status.getParamNull() == 1 && status.getThrowImplication() == null) {
          inferences.put(i, "Normal");
        // AnyException
        } else if (status.getParamNull() == 0 && status.getThrowImplication() == null) {
          inferences.put(i, "Any");
        // SpecificException
        } else if (status.getThrowImplication() != null) {
          String exceptionName = status.getThrowImplication();
          String nullRelated = ":null(";
          if (status.getThrowImplication().startsWith(nullRelated))
            exceptionName = exceptionName.substring(nullRelated.length());
          inferences.put(i, "Specific-" + exceptionName);
        }
      }
    }
    
    return inferences;
  }
}
