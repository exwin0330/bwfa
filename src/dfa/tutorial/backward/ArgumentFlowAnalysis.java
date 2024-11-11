package dfa.tutorial.backward;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class ArgumentFlowAnalysis extends BackwardFlowAnalysis<Unit, Set<String>> {

    private final SootMethod method;

    private static Queue<String> methodNameList = new LinkedList();

    public ArgumentFlowAnalysis(SootMethod method) {
        super(new BriefUnitGraph(method.getActiveBody()));
        this.method = method;
        doAnalysis();
    }

    @Override
    protected Set<String> entryInitialFlow() {
        return new HashSet<>(); // 初期のエントリポイントは空のフロー
    }

    @Override
    protected void flowThrough(Set<String> in, Unit unit, Set<String> out) {
        out.addAll(in);

        if (unit instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) unit;
            Value leftOp = assign.getLeftOp();
            Value rightOp = assign.getRightOp();

            if (rightOp instanceof InvokeExpr) {
                InvokeExpr invokeExpr = (InvokeExpr) rightOp;
                SootMethod invokedMethod = invokeExpr.getMethod();

                String invokedMethodName = invokedMethod.getName();
                System.out.println("Found "+ invokedMethodName +" method call in: " + unit);
                System.out.println("Arguments passed to "+ invokedMethodName + ": " + invokeExpr.getArgs());
                // methodNameList.add(invokedMethodName);

                for (Value arg : invokeExpr.getArgs()) {
                    if (arg instanceof IntConstant) {
                        out.add("Constant argument passed: " + arg);
                    } else {
                        out.add("Variable argument passed: " + arg);
                    }
                }
            }
        } else if (unit instanceof JInvokeStmt) {
            JInvokeStmt invokeStmt = (JInvokeStmt) unit;
            InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
            SootMethod invokedMethod = invokeExpr.getMethod();

            System.out.println("Analyzing arguments passed to " + invokedMethod.getName());
            System.out.println("Arguments: " + invokeExpr.getArgs());
            for (Value arg : invokeExpr.getArgs()) {
                if (arg instanceof IntConstant) {
                    out.add("Constant argument passed to "+ invokedMethod.getName() + ": " + arg);
                } else {
                    out.add("Variable argument passed to "+ invokedMethod.getName() + ": " + arg);
                }
            }
        }
    }

    @Override
    protected void copy(Set<String> source, Set<String> dest) {
        dest.clear();
        dest.addAll(source);
    }

    @Override
    protected void merge(Set<String> in1, Set<String> in2, Set<String> out) {
        out.clear();
        out.addAll(in1);
        out.addAll(in2);
    }

    @Override
    protected Set<String> newInitialFlow() {
        return new HashSet<>();
    }

    public static void main(String[] args) {
        String className = "Calculator";  // 対象クラス名を指定
        setupSoot();

        SootClass sc = Scene.v().loadClassAndSupport(className);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        String mode = "single";
        String targetMethodName = "calculateSum";
        methodNameList.add(targetMethodName);

        // Sootの初期設定
        for (SootMethod method : sc.getMethods()) {
            System.out.println("Method signature: " + method.getSignature());

            // メソッドボディのロード
            if (method.isConcrete()) {
                try {
                    method.retrieveActiveBody(); // メソッドボディを強制ロード
                    System.out.println("Active body loaded for method: " + method.getName());
                } catch (RuntimeException e) {
                    System.err.println("Failed to load body for method: " + method.getName());
                }
            }
        }
        
        for (SootMethod method : sc.getMethods()) {
            if (mode.equals("all") || (mode.equals("single") && method.getName().equals(targetMethodName))) {
                analyze(method);
            }
        }
    }

    private static void analyze(SootMethod method) {
        ArgumentFlowAnalysis analysis = new ArgumentFlowAnalysis(method);
        System.out.println("=== " + method.getName() + " Analysis Results ===");
        for (Unit unit : method.getActiveBody().getUnits()) {
            System.out.println("Unit: " + unit + ", Flow: " + analysis.getFlowAfter(unit));
        }
        System.out.println("=========\n");
    }

    private static void setupSoot() {
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
}
