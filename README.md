# @tComment: Testing Javadoc Comments to Detect Comment-Code Inconsistencies
@tComment automatically generate tests from Javadoc  comments,  specifically  tests for method  properties about null values and related exceptions
#@tComment Design 
@tComment consists of two components, where each component is located in the subdirectories: (1) NullParam, and (2) cloned-randoop

NullParam:
- uses the default Doclet provided with the Javadoc tool to parse the Javadoc comments in a given project
- takes as input the source code for a Java project, automatically analyzes the English text in
the Javadoc comments in the project, and outputs a set of inferred likely properties for each method

cloned-randoop:
- modified Randoop
- takes as input the same code and inferred properties, generates random tests for the methods in the code, checks the inferred properties, and reports inconsistencies.

##Instructions
Below are the instructions for compiling the components in @tComment and to run one of the subject programs in the paper (i.e., ApacheCommonsCollections):<br />
1. To compile the NullParam project:<br />
./run build_doclet

2. To compile cloned-randoop project:<br /> 
./run build_randoop 

3. To download ApacheCommonsCollections project, this will download the project into a subdirectory "programs/collections":<br />
./compile_proj collections

4. Now, ready to run the script for Apache Commons Collections:<br /> 
./run collections -n 0.6 -t 50 -o  

USAGE:<br />
  run [-?dnti]

OPTIONS:<br />
  -d  directory name. Default is Null[$NULLRATIO]Time[$TIMELIMIT]Input[$INPUTLIMIT]_WITH_COMMENTS<br />
  -n  null ratio. Default is 0.5 (double)<br />
  -t  time limit in seconds. Default is 100 (int)<br />
  -i  input limit. Default is 100000000 (int)<br />
  -o  Runs randoop only once<br />
  -r  Runs doclet only<br />
  -?  this usage information<br />

EXAMPLE:<br />
  ./run jodatime -n 0.4 -t 200 -r true<br />


##Output:
There are two kinds of output generated:<br />
1. Inferred null properties. You can refer to all the infer properties for each Java file by using the command: find . -name "*.infer"<br />
2. JUnit Test. At the end of the execution for run command (e.g., ./run collections -n 0.6 -t 50 -o), you will get a message indicating the location for the generated junit test like below:<br />
Created file: $DIR/RandoopTest0.java<br />
...<br />
Created file: $DIR/RandoopTest.java<br />
File RandoopTest[0-9]+.java are the file with specific tests while RandoopTest.java is the test class that include all the generated RandoopTest[0-9]+.java.
