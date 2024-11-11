package dfa.tutorial.flow;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import soot.Body;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;

public class BackwardFlowAnalysisBase extends BackwardFlowAnalysis<Unit, Set<Value>> {
    protected static String sourceDirectory;
    protected static String className;
    protected static String methodName;

    // コンストラクタでコントロールフローフラグメントを受け取る
    public BackwardFlowAnalysisBase(UnitGraph graph, String[] source, String className, String methodName) {
        super(graph);
        setSourceDirectory(source);
        BackwardFlowAnalysisBase.className = className;
        BackwardFlowAnalysisBase.methodName = methodName;

        // 解析の実行
        doAnalysis();
    }

    // 初期化：空のセットを作成
    @Override
    protected Set<Value> newInitialFlow() {
        return new HashSet<>();
    }

    // 出力結果を初期状態として設定（最後の命令に相当）
    @Override
    protected Set<Value> entryInitialFlow() {
        return new HashSet<>();
    }

    // 複数のフロー結果を統合する
    @Override
    protected void merge(Set<Value> in1, Set<Value> in2, Set<Value> out) {
        out.clear();
        out.addAll(in1);
        out.addAll(in2);
    }

    // フロー結果をコピー
    @Override
    protected void copy(Set<Value> source, Set<Value> dest) {
        dest.clear();
        dest.addAll(source);
    }

    // 解析の主なロジック（逆方向の解析を行う）
    @Override
    protected void flowThrough(Set<Value> in, Unit unit, Set<Value> out) {
        // まず、現在のフロー結果をコピー
        out.clear();
        out.addAll(in);

        // 各命令をチェックし、変数の使用や定義を解析
        Stmt stmt = (Stmt) unit;
        if (stmt instanceof DefinitionStmt) {
            DefinitionStmt defStmt = (DefinitionStmt) stmt;
            // 変数が定義される場合はフロー結果から削除
            out.remove(defStmt.getLeftOp());
        }

        // 変数が使われる場合はフロー結果に追加
        for (ValueBox useBox : stmt.getUseBoxes()) {
            out.add(useBox.getValue());
        }
    }

    protected static void init() {
        // Sootの初期化
        G.reset();

        // Sootのオプション設定
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(Scene.v().defaultClassPath());
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
    }

    protected void setSourceDirectory(String[] source) {
        sourceDirectory = System.getProperty("user.dir");
        for (String folder: source) {
            sourceDirectory += File.separator;
            sourceDirectory += folder;
        }
    }

    public static void main(String[] args) {

        init();
        Options.v().set_soot_classpath(sourceDirectory);

        // クラスをロードして解析対象に追加
        SootClass sootClass = Scene.v().loadClassAndSupport(className);
        sootClass.setApplicationClass();

        // 必要なクラスをロード
        Scene.v().loadNecessaryClasses();

        // 解析対象のメソッドを取得
        SootMethod method = sootClass.getMethodByName(methodName);

        // メソッドの制御フローフラフを作成
        Body body = method.retrieveActiveBody();
        UnitGraph graph = new BriefUnitGraph(body);

        // 逆方向フロー解析の実行
        BackwardFlowAnalysisBase analysis = new BackwardFlowAnalysisBase(graph);

        // 結果を出力
        for (Unit unit : graph) {
            System.out.println("Unit: " + unit);
            System.out.println("Before: " + analysis.getFlowBefore(unit));
            System.out.println("After: " + analysis.getFlowAfter(unit));
        }
    }
}
