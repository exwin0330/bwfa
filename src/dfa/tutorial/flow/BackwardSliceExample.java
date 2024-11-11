package dfa.tutorial.flow;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class BackwardSliceExample {
    public static void main(String[] args) {
        String sourceDirectory = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "dfa" +
            File.separator + "tutorial" +
            File.separator + "flow" +
            File.separator + "files";
        String className = "HelperClass";

        // Sootの設定
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory)); // クラスパスを指定
        Options.v().set_whole_program(true);
        Options.v().set_app(true);

        // コールグラフの設定
        Options.v().setPhaseOption("cg.spark", "on");

        SootClass sc = Scene.v().loadClassAndSupport(className);
        sc.setApplicationClass();

        // Sootを初期化
        Scene.v().loadNecessaryClasses();
        SootClass mainClass = Scene.v().getSootClass("MainClass");
        SootClass helperClass = Scene.v().getSootClass("HelperClass");

        // コールグラフの構築
        PackManager.v().runPacks();

        // コールグラフの構築
        CallGraph cg = Scene.v().getCallGraph();
        System.out.println(cg.size());
        SootMethod targetMethod = helperClass.getMethodByName("printMessage");

        // 逆方向に呼び出し元を辿る
        Iterator<Edge> edges = cg.iterator();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (edge.getTgt().method().equals(targetMethod)) {
                SootMethod caller = edge.getSrc().method();
                System.out.println("Caller method: " + caller.getSignature());
            }
        }
    }
}

