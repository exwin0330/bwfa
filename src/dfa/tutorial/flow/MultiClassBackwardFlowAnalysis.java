package dfa.tutorial.flow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import soot.Body;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

public class MultiClassBackwardFlowAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<String>> {

    private FlowSet<String> emptySet;
    private Map<SootMethod, FlowSet<String>> methodFlowResults = new HashMap<>();
    private CallGraph callGraph;

    public MultiClassBackwardFlowAnalysis(UnitGraph graph, CallGraph cg) {
        super(graph);
        this.callGraph = cg;
        // 初期状態のフローセットを作成
        this.emptySet = new ArraySparseSet<>();
        doAnalysis(); // 通常のバックワードフロー解析の実行
    }

    @Override
    protected void flowThrough(FlowSet<String> in, Unit unit, FlowSet<String> out) {
        // ここで1つのメソッド内でのフロー処理を行う
        out.clear();
        out.union(in);

        // ユニットがメソッド呼び出しの場合、呼び出されたメソッドも解析
        if (unit instanceof InvokeStmt) {
            InvokeExpr invokeExpr = ((InvokeStmt) unit).getInvokeExpr();
            SootMethod targetMethod = invokeExpr.getMethod();

            if (!methodFlowResults.containsKey(targetMethod)) {
                analyzeMethod(targetMethod); // 呼び出されたメソッドの解析
            }

            // 呼び出されたメソッドのフローデータを統合
            FlowSet<String> targetMethodFlow = methodFlowResults.get(targetMethod);
            out.union(targetMethodFlow);
        }
    }

    @Override
    protected FlowSet<String> newInitialFlow() {
        return emptySet.clone(); // 新しいフローセットを返す
    }

    @Override
    protected FlowSet<String> entryInitialFlow() {
        return emptySet.clone(); // エントリポイントのフローセットを返す
    }

    @Override
    protected void merge(FlowSet<String> in1, FlowSet<String> in2, FlowSet<String> out) {
        in1.union(in2, out); // フローのマージ
    }

    @Override
    protected void copy(FlowSet<String> source, FlowSet<String> dest) {
        source.copy(dest); // フローセットのコピー
    }

    private void analyzeMethod(SootMethod method) {
        // メソッドのグラフを取得し、そのメソッド内でバックワード解析を行う
        if (!method.isConcrete()) return; // 非具体メソッド（抽象メソッドなど）は無視

        ExceptionalUnitGraph methodGraph = new ExceptionalUnitGraph(method.retrieveActiveBody());
        MultiClassBackwardFlowAnalysis methodAnalysis = new MultiClassBackwardFlowAnalysis(methodGraph, callGraph);

        // 解析結果を保存
        FlowSet<String> methodFlow = methodAnalysis.getFlowAfter(method.getActiveBody().getUnits().getLast());
        methodFlowResults.put(method, methodFlow);
    }

    public Map<SootMethod, FlowSet<String>> getMethodFlowResults() {
        return methodFlowResults;
    }

    public static void main(String[] args) {
        String sourceDirectory = System.getProperty("user.dir") +
        File.separator + "bin" +
        File.separator + "dfa" +
        File.separator + "tutorial" +
        File.separator + "flow" +
        File.separator + "files";

        // Sootの初期化
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_app(true);
        Options.v().set_process_dir(java.util.Collections.singletonList(sourceDirectory));  // アプリケーションのクラスパスを指定
        
        String className = "HelperClass";
        String mainClassName = "MainClass";
        String methodName = "main";
        
        Options.v().set_soot_classpath(sourceDirectory);

        // クラスをロードして解析対象に追加
        SootClass sootClass = Scene.v().loadClassAndSupport(mainClassName);
        //Scene.v().loadClassAndSupport(mainClassName);
        sootClass.setApplicationClass();

        // 必要なクラスをロード
        Scene.v().loadNecessaryClasses();

        // コールグラフの構築
        PackManager.v().runPacks();

        // コールグラフの取得
        CallGraph cg = Scene.v().getCallGraph();

        // 解析対象のメソッドを取得
        SootMethod method = sootClass.getMethodByName(methodName);

        // メソッドの制御フローフラフを作成
        Body body = method.retrieveActiveBody();
        UnitGraph graph = new BriefUnitGraph(body);

        // 逆方向フロー解析の実行
        MultiClassBackwardFlowAnalysis analysis = new MultiClassBackwardFlowAnalysis(graph, cg);

        // 結果を出力
        int count = 0;
        for (Unit unit : graph) {
            count++;
            System.out.println("Unit: " + unit);
            System.out.println("Before: " + analysis.getFlowBefore(unit));
            System.out.println("After: " + analysis.getFlowAfter(unit));
        }
        System.out.println("count:" + count);
    }
}
