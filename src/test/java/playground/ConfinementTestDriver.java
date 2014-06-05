package playground;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.ClassRepository;
import de.seerhein_lab.jic.EmercencyBrakeException;
import de.seerhein_lab.jic.Utils;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.StringAnnotation;

public class ConfinementTestDriver {
	private static final String LOGFILEPATH = "log.txt";
	private static Logger logger;
	// private static Set<EvaluationResult> results = new
	// HashSet<EvaluationResult>();
	private static Set<String> stackConfinedClasses = new TreeSet<String>();
	private static Set<String> notStackConfinedClasses = new TreeSet<String>();
	private static Set<String> notAnalyzedClasses = new TreeSet<String>();
	private static Set<String> notInstantiatedClasses = new TreeSet<String>();

	public static void main(String[] args) throws ClassNotFoundException, SecurityException,
			IOException {
		logger = Utils.setUpLogger("ConfinementTestDriver", LOGFILEPATH, Level.SEVERE);

		// String class_name =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest";
		// String classToAnalyze =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";

		// Set<JavaClass> classes =
		// ClassRepository.getClassWithInnerClasses(class_name);

		// String classToAnalyze =
		// "de.seerhein_lab.jic.analyzer.StackConfinementAcceptanceTest$TestClass";
		Collection<JavaClass> classes = ClassRepository.getClasses("de.seerhein_lab.jic");

		// Set<AnalysisResult> analysisResults = analyze(classToAnalyze,
		// classes);

		analyzeAllClasses(classes);

		// logResults(classToAnalyze, analysisResults);

	}

	private static void logResults(String classToAnalyze, Set<AnalysisResult> analysisResults) {
		boolean stackConfined = true;
		logger.severe(String
				.format("\n###############################################################################\n"
						+ "#    %-65s        #"
						+ "\n#-----------------------------------------------------------------------------#\n#",
						classToAnalyze));
		// logger.severe("Stack Confinement of " + classToAnalyze);
		for (AnalysisResult analysisResult : analysisResults) {
			if (analysisResult.getBugs().isEmpty()) {
				logger.severe("#\t Stack Confined\t\t : " + analysisResult.getAnalyzedMethod());
			} else {
				for (BugInstance bugInstance : analysisResult.getBugs()) {
					MethodAnnotation method = bugInstance.getAnnotationWithRole(
							MethodAnnotation.class, "METHOD_DEFAULT");
					logger.severe("#\t NOT Stack Confined\t : " + method);
					logger.severe("#\t\t (Line "
							+ method.getSourceLines().getStartLine()
							+ ") -> "
							+ bugInstance.getAnnotationWithRole(StringAnnotation.class,
									"STRING_DEFAULT"));
				}
				stackConfined = false;
			}
		}
		logger.severe(String
				.format("#\n#-----------------------------------------------------------------------------#\n"
						+ "#    %-70s   #"
						+ "\n###############################################################################\n",
						classToAnalyze
								+ (stackConfined ? ":   Stack Confined" : ":   NOT Stack Confined")));

		if (analysisResults.isEmpty())
			notInstantiatedClasses.add(classToAnalyze);
		else if (stackConfined)
			stackConfinedClasses.add(classToAnalyze);
		else
			notStackConfinedClasses.add(classToAnalyze);

	}

	public static Set<AnalysisResult> analyze(String classToCheck,
			Collection<JavaClass> classesToAnalyze) {
		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		return ClassRepository.analyzeMethods(repository.getClass(classToCheck));
	}

	public static void analyzeAllClasses(Collection<JavaClass> classesToAnalyze) {
		ClassRepository repository = new ClassRepository();

		repository.analyzeClasses(classesToAnalyze);

		for (JavaClass javaClass : classesToAnalyze) {
			try {
				logResults(javaClass.getClassName(), ClassRepository.analyzeMethods(repository
						.getClass(javaClass.getClassName())));
			} catch (EmercencyBrakeException e) {
				notAnalyzedClasses.add(javaClass.getClassName());
			}
		}

		logSummary("Stack Confined Classes", stackConfinedClasses);
		logSummary("NOT Stack Confined Classes", notStackConfinedClasses);
		logSummary("NOT Instantiated Classes", notInstantiatedClasses);
		logSummary("Not analyzed Classes", notAnalyzedClasses);
	}

	private static void logSummary(String list, Set<String> classes) {
		logger.severe(String
				.format("\n###############################################################################\n"
						+ "#    %2d   %-60s        #"
						+ "\n#-----------------------------------------------------------------------------#",
						classes.size(), list));
		for (String clazz : classes) {
			logger.severe(String.format("#    %-65s        #", clazz));
		}
		logger.severe("###############################################################################\n");
	}
}
