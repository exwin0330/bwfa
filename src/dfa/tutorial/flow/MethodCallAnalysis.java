package dfa.tutorial.flow;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

public class MethodCallAnalysis {
    private AnalysisGraph graph = new AnalysisGraph();
    private Set<String> visitedMethods = new HashSet<>();
    private Queue<String> methodQueue = new LinkedList<>();
    private String className;

    public MethodCallAnalysis(String className) {
        setupSoot();
        this.className = className;

        SootClass sc = Scene.v().loadClassAndSupport(this.className);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
    }

    private void setupSoot() {
        String sourceDirectory = System.getProperty("user.dir") +
            File.separator + "bin" +
            File.separator + "dfa" +
            File.separator + "tutorial" +
            File.separator + "flow" +
            File.separator + "files";

        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_java);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_app(true);
        Scene.v().loadBasicClasses();
    }

    public void analyze(String methodName) {
        methodQueue.add(methodName);

        while (!methodQueue.isEmpty()) {
            String currentMethod = methodQueue.poll();
            if (visitedMethods.contains(currentMethod)) continue;

            System.out.println("=== Analyzing method: " + currentMethod + " ===");
            visitedMethods.add(currentMethod);
            runAnalysis(currentMethod);
        }
    }

    public void runAnalysis(String methodName) {
        // メソッドの呼び出しを探す
        PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new BodyTransformer() {
            @Override
            protected void internalTransform(Body body, String phase, Map<String, String> options) {
                SootMethod method = body.getMethod();
                String methodNodeLabel = "Method: " + method.getName();
                graph.getOrCreateNode(methodNodeLabel);

                BriefUnitGraph cfg = new BriefUnitGraph(body);
                SimpleLocalDefs localDefs = new SimpleLocalDefs(cfg);
                
                for (Unit unit : body.getUnits()) {
                    if (!(unit instanceof InvokeStmt || (unit instanceof AssignStmt && ((AssignStmt) unit).containsInvokeExpr()))) {
                        continue;
                    }
                    // 各ステートメントをノードとして追加
                    String stmtNodeLabel = "Stmt: " + unit.toString();
                    GraphNode stmtNode = graph.getOrCreateNode(stmtNodeLabel);

                    InvokeExpr invokeExpr = (unit instanceof InvokeStmt) ?
                            ((InvokeStmt) unit).getInvokeExpr() :
                            ((AssignStmt) unit).getInvokeExpr();

                    // メソッド呼び出しのノードとエッジを追加
                    String invokedMethodLabel = "Method: " + invokeExpr.getMethod().getName();
                    graph.addEdge(stmtNodeLabel, invokedMethodLabel, "calls");

                    // 各引数を追跡
                    for (Value arg : invokeExpr.getArgs()) {
                        if (arg instanceof Local) {
                            traceDefinition((Local) arg, unit, stmtNodeLabel, localDefs);
                        }
                    }
                }
            }

            private void traceDefinition(Local local, Unit unit, String stmtNodeLabel, SimpleLocalDefs localDefs) {
                for (Unit defUnit : localDefs.getDefsOfAt(local, unit)) {
                    String defNodeLabel = "Stmt: " + defUnit.toString();
                    graph.addEdge(stmtNodeLabel, defNodeLabel, "defined-by");

                    if (defUnit instanceof AssignStmt) {
                        AssignStmt assignStmt = (AssignStmt) defUnit;
                        Value rightOp = assignStmt.getRightOp();

                        if (rightOp instanceof Local) {
                            traceDefinition((Local) rightOp, defUnit, defNodeLabel, localDefs);
                        }
                    }
                }
            }
        }));

        PackManager.v().runPacks();
        PackManager.v().getPack("jtp").remove("jtp.myTransform");
    
        graph.printGraph();
    }

    public static void main(String[] args) {
        MethodCallAnalysis methodCallAnalysis = new MethodCallAnalysis("Calculator");
        methodCallAnalysis.analyze("add");
    }
}
