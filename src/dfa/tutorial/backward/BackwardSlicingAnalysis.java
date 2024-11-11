package dfa.tutorial.backward;

import java.util.ArrayList;
import java.util.List;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

public class BackwardSlicingAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<Object>> {
    private final List<String> reverseFlowLog = new ArrayList<>();  // 逆順のフローを保存するリスト

    public BackwardSlicingAnalysis(SootMethod method) {
        super(new ExceptionalUnitGraph(method.retrieveActiveBody()));
        doAnalysis();
        printReverseFlow();
    }

    // 初期値を定義
    @Override
    protected FlowSet<Object> newInitialFlow() {
        return new ArraySparseSet<>();
    }

    // 出口条件を定義
    @Override
    protected FlowSet<Object> entryInitialFlow() {
        return new ArraySparseSet<>();
    }

    // 各ユニット（命令）ごとの処理を定義
    @Override
    protected void flowThrough(FlowSet<Object> in, Unit unit, FlowSet<Object> out) {
        out.union(in);

        // 変数の使用を逆方向に追跡し、ログに追加
        unit.getUseBoxes().forEach(useBox -> {
            if (useBox.getValue() instanceof Local) {
                Local local = (Local) useBox.getValue();
                out.add(local);
                reverseFlowLog.add("Unit: " + unit + ", Variable: " + local.getName());  // ログに追加
            }
        });
    }

    // データフローのマージ処理
    @Override
    protected void merge(FlowSet<Object> in1, FlowSet<Object> in2, FlowSet<Object> out) {
        in1.union(in2, out);
    }

    // 値のコピー処理
    @Override
    protected void copy(FlowSet<Object> source, FlowSet<Object> dest) {
        source.copy(dest);
    }

    // 逆順にフローを出力
    private void printReverseFlow() {
        System.out.println("=== Reverse Variable Flow ===");
        for (int i = reverseFlowLog.size() - 1; i >= 0; i--) {
            System.out.println(reverseFlowLog.get(i));
        }
    }
}
