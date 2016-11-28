# @tComment: Testing Javadoc Comments to Detect Comment-Code Inconsistencies

@tComment automatically generate tests from Javadoc  comments,  specifically  tests for method  properties about null values and related exceptions

#@tComment Design 

@tComment consists of two components, where each component is located in the subdirectories: (1) NullParam, and (2) cloned-randoop

NullParam:
- uses the default Doclet provided with the Javadoc tool to parse the Javadoc comments in a given project
- takes as input the source code for a Java project, automatically analyzes the English text in
the Javadoc comments in the project, and outputs a set of inferred likely properties for each method

cloned-randoop:
- modified Randoop (obtained from Randoop version 1.3.1)
- takes as input the same code and inferred properties, generates random tests for the methods in the code, checks the inferred properties, and reports inconsistencies.

##Instructions for Compilation: 
Below are the instructions for compiling the components in @tComment in LINUX:

1. To compile the NullParam project: `./run build_doclet`
2. To compile cloned-randoop project: `./run build_randoop`

##Instructions for Running Example Program
To run one of the subject programs in the paper (i.e., ApacheCommonsCollections):

1. To download ApacheCommonsCollections project, this will download the project into a subdirectory `programs/collections`: `./compile_proj collections`
2. Now, ready to run the script for Apache Commons Collections: `./run collections -n 0.6 -t 50 -o`

```
USAGE:
  run [-?dnti]

OPTIONS:
  -d  directory name. Default is Null[$NULLRATIO]Time[$TIMELIMIT]Input[$INPUTLIMIT]_WITH_COMMENTS
  -n  null ratio. Default is 0.5 (double)
  -t  time limit in seconds. Default is 100 (int)
  -i  input limit. Default is 100000000 (int)
  -o  Runs randoop only once
  -r  Runs doclet only
  -?  this usage information

EXAMPLE:
  ./run jodatime -n 0.4 -t 200 -r true
```

##Output:

There are two kinds of output generated:

1. Inferred null properties. You can refer to all the infer properties for each Java file by using the command: `find . -name "*.infer"`  
2. JUnit Test. At the end of the execution for run command (e.g., `./run collections -n 0.6 -t 50 -o`), you will get a message indicating the location for the generated junit test like below:

```
Created file: $DIR/RandoopTest0.java
...
Created file: $DIR/RandoopTest.java
```

File RandoopTest[0-9]+.java are the file with specific tests while RandoopTest.java is the test class that include all the generated RandoopTest[0-9]+.java.

##Running @tComment for new subject
1. Refer to `compile_proj` script for example of writing the compilation script for the new subject
2. Refer to `run` script for supporting new subject. Note that the function `set_program_specific_variables()` in `run` script requires a few variables to be set: `SRCPATH`(location for the src folder), `PROJBIN`(location for the compiled Java classes),`PROJLIB`(if the new subject depends on external library, usually in a separate lib folder) and `PACKAGES`(Java packages, separated by space for multiple packages). This variable allows the doclet to detect the location for the project files correctly. The default (less accurate in detecting the location for project files) is to use a file "$projectname-classes.txt" that contains for each lines, full class name (package name together with class name) for each Java classess that you want @tComment to test. You could use the following command for obtaining the list of class name for a particular folder (assuming you run the command for the root folder that is a valid package (e.g., if you have statements like `import org.*` for importing the package in your Java classes, then `org` is the root folder) ):
`find $foldername -name "*.java" | sed -e 's/\.java/\.class/g;s/\//\./g'`


*Note that as @tComment only analyzes null-related properties, if all Javadoc comments in your Java project do not mention any null related information, @tComment will work the same as the unmodified Randoop.

##Citing @tComment

If you use @tComment in an academic work, we would be really glad if you cite our paper using the following bibtex:

```

  @InProceedings{TanETAL2012atComment,
    author = {Shin Hwei Tan and Darko Marinov and Lin Tan and Gary T. Leavens},
    title = {{@tComment}: Testing {Javadoc} Comments to Detect Comment-Code Inconsistencies},
    booktitle = {Proceedings of the Fifth IEEE International Conference on Software Testing, Verification and Validation (ICST 2012)},
    pages = {260--269},
    month = Apr,
    year = 2012,
    address = {Montreal, Canada}
  }
```

