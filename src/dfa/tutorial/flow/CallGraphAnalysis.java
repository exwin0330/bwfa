package dfa.tutorial.flow;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class CallGraphAnalysis {
    public static String className;
    
    public static void main(String[] args) {
        // Sootの初期設定
        setupSoot();
        SootClass sc = Scene.v().getSootClass(className);

        // ターゲットとなるメソッド（今回は add(int, int)）
        SootMethod targetMethod = findTargetMethod(sc, "add", "int", "int");

        if (targetMethod != null) {
            System.out.println("ターゲットメソッド: " + targetMethod);
            // 呼び出し元を検出して表示
            findCallers(targetMethod);
        } else {
            System.out.println("ターゲットメソッドが見つかりません。");
        }
    }

    // Sootの初期設定
    private static void setupSoot() {
        String sourceDirectory = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "dfa" +
            File.separator + "tutorial" +
            File.separator + "flow" +
            File.separator + "files";
        className = "Calculator";

        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        Options.v().set_include(Collections.singletonList("dfa.tutorial.flow.files"));
        Options.v().set_exclude(Arrays.asList("java.*", "javax.*", "jdk.*", "sun.*"));
        System.out.println("set soot classpath: " + sourceDirectory);

        // コールグラフの設定
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().setPhaseOption("cg.cha", "on");
        Options.v().setPhaseOption("cg.spark", "vbta:true");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");

        SootClass sc = Scene.v().loadClassAndSupport(className);
        System.out.println("loadClassAndSupport: " + className);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        // PackManagerにコールグラフ生成を追加
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, java.util.Map<String, String> options) {
                // Sparkを使ったコールグラフの生成
                soot.jimple.spark.SparkTransformer.v().transform();
            }
        }));

        // 必要なフェーズを適用
        PackManager.v().runPacks();
    }

    // ターゲットメソッドを探すメソッド
    private static SootMethod findTargetMethod(SootClass sootClass, String methodName, String... paramTypes) {
        for (SootMethod method : sootClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                // パラメータの型が一致するか確認
                if (method.getParameterCount() == paramTypes.length) {
                    boolean match = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (!method.getParameterType(i).toString().equals(paramTypes[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return method; // 正しいメソッドが見つかった
                    }
                }
            }
        }
        return null;
    }

    // 呼び出し元を検出して表示
    private static void findCallers(SootMethod targetMethod) {
        CallGraph cg = Scene.v().getCallGraph();
        Iterator<Edge> edges = cg.edgesInto(targetMethod);

        outputCallGraph(cg);

        // 呼び出し元メソッドを探索
        if (!edges.hasNext()) {
            System.out.println("呼び出し元が見つかりませんでした。");
        }

        // 呼び出し元メソッドを探索
        while (edges.hasNext()) {
            SootMethod caller = edges.next().src();
            System.out.println("呼び出し元メソッド: " + caller);

            // 再帰的に上位の呼び出し元を探す
            findCallers(caller);
        }
    }

    private static void outputCallGraph(CallGraph cg) {
        // コールグラフのすべてのエッジを出力
        System.out.println("=== Call Graph ===");
        Iterator<Edge> edges = cg.iterator();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod src = edge.getSrc().method();  // 呼び出し元メソッド
            SootMethod tgt = edge.getTgt().method();  // 呼び出し先メソッド

            // 呼び出し元メソッドがアプリケーション内のクラスかどうかをチェック
            if (!isExternalMethod(src)) {
                System.out.println(src.getSignature() + " -> " + tgt.getSignature());
            }
        }
    }

    // メソッドが外部クラスに属しているかどうかを判定
    private static boolean isExternalMethod(SootMethod method) {
        String className = method.getDeclaringClass().getName();
        // "java" や "javax" などで始まるクラスは外部のJDKのクラス
        return className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("jdk.") || isThirdPartyLibrary(method);
    }

    // 外部ライブラリかどうかを判定するための例示的なメソッド
    private static boolean isThirdPartyLibrary(SootMethod method) {
        String className = method.getDeclaringClass().getName();
        // ここにサードパーティライブラリのパッケージ名を指定
        return className.startsWith("org.") || className.startsWith("sun.") || className.startsWith("com.thirdpartylibrary.");
    }
}
