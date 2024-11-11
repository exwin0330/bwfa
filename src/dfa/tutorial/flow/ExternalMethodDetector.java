package dfa.tutorial.flow;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class ExternalMethodDetector {

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

        // コールグラフ生成のためのオプション設定
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().setPhaseOption("cg", "verbose:true");
        Options.v().setPhaseOption("cg", "types-for-invoke:true");

        // エントリーポイントを指定
        SootClass mainClass = Scene.v().loadClassAndSupport("MainClass");
        SootClass helperClass = Scene.v().loadClassAndSupport("HelperClass");
        Scene.v().setMainClass(mainClass);

        List<SootMethod> entryPoints = new ArrayList<>();
        entryPoints.add(Scene.v().getMethod("<MainClass: void main(java.lang.String[])>"));
        entryPoints.add(Scene.v().getMethod("<MainClass: java.lang.String message3()>"));
        entryPoints.add(Scene.v().getMethod("<MainClass: java.lang.String message2()>"));
        Scene.v().setEntryPoints(entryPoints);

        // JDKやサードパーティライブラリを除外
        Options.v().set_exclude(Arrays.asList("java.", "javax.", "sun.", "org.", "com.thirdpartylibrary."));
        Options.v().set_no_bodies_for_excluded(true);  // 除外されたクラスの本体を読み込まない

        // Sootクラスを解析対象に設定
        Scene.v().loadNecessaryClasses();

        // コールグラフの構築
        PackManager.v().runPacks();

        // コールグラフの取得
        CallGraph cg = Scene.v().getCallGraph();

        // DFSを使用したコールグラフの探索（探索済みノードを記録するSet）
        Set<SootMethod> visited = new HashSet<>();
        Stack<SootMethod> stack = new Stack<>();
        stack.push(mainClass.getMethodByName("main")); // エントリーポイント（mainメソッド）をスタックに追加

        int count = 0;
        while (!stack.isEmpty()) {
            SootMethod currentMethod = stack.pop();

            // 外部メソッドの場合、そのノードの探索を打ち切る
            if (isExternalMethod(currentMethod)) {
                System.out.println("Skipping external method: " + currentMethod.getSignature());
                continue; // 子ノードを探索しない
            }

            // 内部メソッドの場合、その呼び出しを表示
            if (!isExternalMethod(currentMethod)) {
                System.out.println("Analyzing method: " + currentMethod.getSignature());
            }
            count++;

            // 訪問済みでないノードをスタックに追加
            if (!visited.contains(currentMethod)) {
                visited.add(currentMethod);
                // 子ノード（呼び出し先メソッド）をスタックに追加
                for (Iterator<Edge> it = cg.edgesOutOf(currentMethod); it.hasNext(); ) {
                    Edge edge = it.next();
                    SootMethod tgtMethod = edge.tgt();
                    if (!visited.contains(tgtMethod)) {
                        stack.push(tgtMethod);
                    }
                }
            }
        }
        System.out.println("count:" + count);
    }

    // メソッドが外部クラスに属しているかどうかを判定
    private static boolean isExternalMethod(SootMethod method) {
        String className = method.getDeclaringClass().getName();
        // "java" や "javax" などで始まるクラスは外部のJDKのクラス
        return className.startsWith("java.") || className.startsWith("javax.") || isThirdPartyLibrary(method);
    }

    // 外部ライブラリかどうかを判定するための例示的なメソッド
    private static boolean isThirdPartyLibrary(SootMethod method) {
        String className = method.getDeclaringClass().getName();
        // ここにサードパーティライブラリのパッケージ名を指定
        return className.startsWith("org.") || className.startsWith("com.thirdpartylibrary.");
    }
}
