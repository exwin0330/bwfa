package dfa.tutorial.flow;

import java.io.File;
import java.util.Collections;
import java.util.List;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.options.Options;

public class MethodDependencyAnalysis {
    public static void main(String[] args) {
        String sourceDirectory = System.getProperty("user.dir") +
        File.separator + "bin" +
        File.separator + "dfa" +
        File.separator + "tutorial" +
        File.separator + "flow" +
        File.separator + "files";
    
        // Sootの設定
        Options.v().set_prepend_classpath(true);
        Options.v().set_src_prec(Options.src_prec_java);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));  // Javaファイルのパス
        Options.v().set_output_format(Options.output_format_jimple);  // Jimple形式で出力

        // 対象クラスのロード
        SootClass sc = Scene.v().loadClassAndSupport("Calculator");
        Scene.v().loadNecessaryClasses();

        // メソッド間の依存関係解析
        analyzeMethodDependencies(sc);

        // Sootの実行
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }

    // クラス内のメソッド間の依存関係を解析するメソッド
    private static void analyzeMethodDependencies(SootClass sc) {
        System.out.println("クラス: " + sc.getName());

        // クラス内のメソッドを取得
        List<SootMethod> methods = sc.getMethods();
        for (SootMethod method : methods) {
            System.out.println("メソッド: " + method.getName());
            if (method.isConcrete()) {
                Body body = method.retrieveActiveBody();

                // メソッド内で呼び出される他のメソッドを解析
                for (Unit unit : body.getUnits()) {
                    for (ValueBox vb : unit.getUseBoxes()) {
                        Value value = vb.getValue();
                        if (value instanceof InvokeExpr) {
                            InvokeExpr invokeExpr = (InvokeExpr) value;
                            SootMethod invokedMethod = invokeExpr.getMethod();
                            if (!invokedMethod.getDeclaringClass().getName().equals("soot.dummy.InvokeDynamic")) {
                                System.out.println("  呼び出されているメソッド: " + invokedMethod.getName() + " (クラス: " + invokedMethod.getDeclaringClass().getName() + ")");
                            }
                        }
                    }
                }
            }
        }
    }
}
