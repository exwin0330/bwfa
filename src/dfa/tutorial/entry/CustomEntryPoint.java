import java.util.ArrayList;
import java.util.List;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class CustomEntryPoint {
    public static void main(String[] args) {
        // Sootの初期化
        G.reset();

        // クラスパスの設定 (例: プロジェクトのbinディレクトリ)
        String classpath = "bin";
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(classpath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        // 解析対象のクラスをロード
        String className = "example.MyTestClass";
        SootClass targetClass = Scene.v().loadClassAndSupport(className);
        targetClass.setApplicationClass();

        // カスタムエントリポイントを指定 (例: "targetMethod" メソッド)
        SootMethod entryPointMethod = targetClass.getMethodByName("targetMethod");

        // エントリポイントをリストとして作成
        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(entryPointMethod);

        // エントリポイントをSootに設定
        Scene.v().setEntryPoints(entryPoints);

        // 必要なクラスをロード
        Scene.v().loadNecessaryClasses();

        // コールグラフの構築やデータフロー解析などを行う
        PackManager.v().runPacks();

        // 結果の出力（例: コールグラフの出力）
        CallGraph cg = Scene.v().getCallGraph();
        for (Edge edge : cg) {
            System.out.println(edge.getSrc() + " -> " + edge.getTgt());
        }
    }
}
