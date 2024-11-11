package dfa.tutorial.flow;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

public class MultiMethodBackwardFlowAnalysis {
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
    
        // クラスファイルのディレクトリを指定
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));

        // コールグラフの設定
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg", "all-reachable:true");

        // エントリーポイントの設定
        SootClass mainClass = Scene.v().loadClassAndSupport("mainClass");
        SootClass helperClass = Scene.v().loadClassAndSupport("HelperClass");

        Scene.v().setMainClass(mainClass);
        SootMethod entryPoint = mainClass.getMethodByName("main");
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));

        // Sootクラスを解析対象に設定
        Scene.v().loadNecessaryClasses();

        // コールグラフの構築
        PackManager.v().runPacks();

        // 解析対象のメソッド
        SootMethod targetMethod = Scene.v().getMethod("<HelperClass: void printMessage()>");

        // コールグラフの取得
        CallGraph cg = Scene.v().getCallGraph();

        /*

        // メソッドの呼び出し元を逆方向に辿る
        System.out.println("Backward traversal from method: " + targetMethod.getSignature());
        Set<SootMethod> visited = new HashSet<>();  // 既に訪れたメソッドを記録

        backwardTraversal(cg, targetMethod, visited);
        */

        // デバッグ用
        searchCallGraph(mainClass, cg);
    }

    // 逆方向にコールグラフをたどる
    private static void backwardTraversal(CallGraph cg, SootMethod method, Set<SootMethod> visited) {
        // 呼び出し元のエッジを取得
        Iterator<Edge> edgesInto = cg.edgesInto(method);

        // 再帰的に呼び出し元を辿る
        while (edgesInto.hasNext()) {
            Edge edge = edgesInto.next();
            SootMethod srcMethod = edge.src();  // 呼び出し元のメソッド

            // 既に訪問済みの場合はスキップ
            if (!visited.contains(srcMethod)) {
                visited.add(srcMethod);  // 訪問済みメソッドとしてマーク

                System.out.println("Called from: " + srcMethod.getSignature());

                // 再帰的にさらに上の呼び出し元を探索
                backwardTraversal(cg, srcMethod, visited);
            }
        }
    }

    // DFSを使用したコールグラフの探索（探索済みノードを記録するSet）
    public static void searchCallGraph(SootClass mainClass, CallGraph cg) {
        Set<SootMethod> visited = new HashSet<>();
        Stack<SootMethod> stack = new Stack<>();
        stack.push(mainClass.getMethodByName("main")); // エントリーポイント（mainメソッド）をスタックに追加

        int count = 0;
        while (!stack.isEmpty()) {
            SootMethod currentMethod = stack.pop();

            // 外部メソッドの場合、そのノードの探索を打ち切る
            if (isExternalMethod(currentMethod)) {
                //System.out.println("Skipping external method: " + currentMethod.getSignature());
                continue; // 子ノードを探索しない
            }

            // 内部メソッドの場合、その呼び出しを表示
            System.out.println("Analyzing method: " + currentMethod.getSignature());
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
