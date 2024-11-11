package dfa.tutorial.backward;

import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;

public class RecursiveVariableFlowAnalysis extends VariableFlowAnalysis {
    private final SootMethod method;

    public RecursiveVariableFlowAnalysis(SootMethod method) {
        super(method);
        this.method = method;
    }

    protected void flowThrough(Set<String> in, Unit unit, Set<String> out) {
        out.addAll(in);
        
        if (unit instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) unit;
            Value leftOp = assign.getLeftOp();
            out.add(leftOp.toString());
        }

        // Returnステートメントを解析
        if (unit instanceof ReturnStmt) {
            ReturnStmt returnStmt = (ReturnStmt) unit;
            Value returnValue = returnStmt.getOp();
            out.add("Return value calculated: " + returnValue);
        }

        // メソッド呼び出しがある場合、再帰的にそのメソッドを解析
        if (unit instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) unit;
            if (assign.containsInvokeExpr()) {
                InvokeExpr invokeExpr = assign.getInvokeExpr();
                SootMethod invokedMethod = invokeExpr.getMethod();
                System.out.println("Analyzing invoked method: " + invokedMethod.getSignature());

                if (invokedMethod.isConcrete()) {
                    invokedMethod.retrieveActiveBody();
                    RecursiveVariableFlowAnalysis nestedAnalysis = new RecursiveVariableFlowAnalysis(invokedMethod);
                    out.add("Invoked method analysis for " + invokedMethod.getSignature() + ": " + nestedAnalysis.getResults());
                }
            }
        }
    }

    public Set<String> getResults() {
        return getFlowAfter(method.getActiveBody().getUnits().getFirst());
    }

    public static void main(String[] args) {
        String className = "Calculator";  // 対象クラス名を指定
        setupSoot();

        SootClass sc = Scene.v().loadClassAndSupport(className);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

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

        SootMethod targetMethod = sc.getMethodByName("calculateSum"); // 解析対象メソッドを指定

        RecursiveVariableFlowAnalysis analysis = new RecursiveVariableFlowAnalysis(targetMethod);
        analysis.doAnalysis();
        
        Set<String> entryFlow = analysis.entryInitialFlow();
        System.out.println("エントリポイントのフロー: " + entryFlow);
        System.out.println("=== Analysis Results ===");
        for (Unit unit : targetMethod.getActiveBody().getUnits()) {
            System.out.println("Unit: " + unit + ", Flow: " + analysis.getFlowAfter(unit));
        }
    }
}
