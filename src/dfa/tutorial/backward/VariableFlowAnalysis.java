package dfa.tutorial.backward;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class VariableFlowAnalysis extends BackwardFlowAnalysis<Unit, Set<String>> {

    public VariableFlowAnalysis(SootMethod method) {
        super(new BriefUnitGraph(method.getActiveBody()));
    }

    @Override
    protected void flowThrough(Set<String> in, Unit unit, Set<String> out) {
        // このメソッド内でフローを計算
        Set<String> result = new HashSet<>(in);
        
        // ユニットに関連する変数を追加
        if (unit instanceof AssignStmt) {
            AssignStmt assign = (AssignStmt) unit;
            Value leftOp = assign.getLeftOp();
            result.add(leftOp.toString());
        
            // デバッグ用出力
            System.out.println("Unit: " + unit + ", Variable: " + leftOp);
        }

        out.clear();
        out.addAll(result);  // 計算結果をoutにコピーして反映
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

    public static void main(String[] args) {
        String className = "Calculator";
        setupSoot();

        // 対象クラスをロード
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
        SootMethod targetMethod = sc.getMethod("void main(java.lang.String[])");
        
        // 逆流解析を実行
        VariableFlowAnalysis analysis = new VariableFlowAnalysis(targetMethod);
        analysis.doAnalysis();
        
        Set<String> entryFlow = analysis.entryInitialFlow();
        System.out.println("エントリポイントのフロー: " + entryFlow);

        System.out.println("=== Analysis Results ===");
        for (Unit u : new BriefUnitGraph(targetMethod.getActiveBody())) {
            System.out.println("Unit: " + u + ", Flow: " + analysis.getFlowAfter(u));
        }
    }

    @Override
    protected Set<String> entryInitialFlow() {
        return new HashSet<>(); // エントリポイントでは特に流れはない
    }

    @Override
    protected Set<String> newInitialFlow() {
        return new HashSet<>();
    }

    protected static void setupSoot() {
        String sourceDirectory = System.getProperty("user.dir") +
            File.separator + "bin" +
            File.separator + "dfa" +
            File.separator + "tutorial" +
            File.separator + "flow" +
            File.separator + "files";

        // Sootの基本設定
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_java);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_app(true);
        Scene.v().loadBasicClasses();
    }
}
