package dfa.tutorial.backward;

import java.io.File;
import java.util.Collections;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;

public class BackwardSlicingExample {
    public static void main(String[] args) {
        String className = "Calculator";
        setupSoot();

        // 対象クラスをロード
        SootClass sc = Scene.v().loadClassAndSupport(className);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        SootMethod method = sc.getMethodByName("add");

        // バックワードフロー解析の実行
        BackwardSlicingAnalysis analysis = new BackwardSlicingAnalysis(method);

        /*
        // メソッド依存関係解析
        for (SootMethod method : sc.getMethods()) {
            if (method.isConcrete()) {
                Body body = method.retrieveActiveBody();
                UnitGraph graph = new BriefUnitGraph(body);  // メソッドの制御フローグラフを作成
                
                // バックワード解析を行う
                BackwardSlicingAnalysis analysis = new BackwardSlicingAnalysis(graph);
                System.out.println("バックワードスライス: " + method.getName());
                
                // メソッドのユニットを解析し、スライス情報を出力
                for (Unit unit : body.getUnits()) {
                    System.out.println("  命令: " + unit);
                    FlowSet<Object> slice = analysis.getFlowBefore(unit);
                    System.out.println("  依存関係（スライス）: " + slice);
                }
            }
        }

        // Sootの実行
        PackManager.v().runPacks();
        */
    }

    private static void setupSoot() {
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
        Scene.v().loadBasicClasses();
    }
}
