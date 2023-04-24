package valer.sootnp

import kotlinx.coroutines.runBlocking
import soot.Local
import soot.Unit
import soot.jimple.*
import soot.toolkits.graph.DirectedGraph
import soot.toolkits.scalar.ForwardFlowAnalysis
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class NullPointerAnalysis(graph: DirectedGraph<*>?, var analysisMode: AnalysisMode) :
    ForwardFlowAnalysis<Unit?, NullFlowSet?>(graph as DirectedGraph<Unit?>?) {
    enum class AnalysisMode {
        MUST, MAY_P, MAY_O
    }

    private val lock = ReentrantLock()

    init {
        lock.withLock { doAnalysis() }

    }

    override fun flowThrough(`in`: NullFlowSet?, d: Unit?, out: NullFlowSet?) {
        `in`?.copy(out)
        if (d != null) {
            if (out != null) {
                kill(`in`, d, out)
            }
        }
        if (out != null) {
            if (`in` != null) {
                if (d != null) {
                    generate(`in`, d, out)
                }
            }
        }
    }

    override fun newInitialFlow(): NullFlowSet {
        return NullFlowSet()
    }

    override fun merge(in1: NullFlowSet?, in2: NullFlowSet?, out: NullFlowSet?) {
        if (analysisMode != AnalysisMode.MUST) in1!!.union(in2, out) else in1!!.intersection(in2, out)
    }

    override fun copy(source: NullFlowSet?, dest: NullFlowSet?) {
        source!!.copy(dest)
    }

    protected fun kill(inSet: NullFlowSet?, unit: Unit, outSet: NullFlowSet) {
        unit.apply(object : AbstractStmtSwitch() {
            override fun caseAssignStmt(stmt: AssignStmt) {
                try {
                    val leftOp = stmt.leftOp as Local
                    outSet.remove(leftOp)
                } catch (e: Exception) {
                }

            }
        })
    }

    protected fun generate(inSet: NullFlowSet, unit: Unit, outSet: NullFlowSet) {
        unit.apply(object : AbstractStmtSwitch() {
            override fun caseAssignStmt(stmt: AssignStmt) {
                try {
                    val leftOp = stmt.leftOp as Local
                    stmt.rightOp.apply(object : AbstractJimpleValueSwitch() {
                        override fun caseLocal(v: Local) {
                            if (inSet.contains(v)) outSet.add(leftOp)
                        }

                        override fun caseNullConstant(v: NullConstant) {
                            outSet.add(leftOp)
                        }

                        override fun caseInterfaceInvokeExpr(v: InterfaceInvokeExpr) {
                            if (analysisMode == AnalysisMode.MAY_P) outSet.add(leftOp)
                        }

                        override fun caseStaticInvokeExpr(v: StaticInvokeExpr) {
                            if (analysisMode == AnalysisMode.MAY_P) outSet.add(leftOp)
                        }

                        override fun caseVirtualInvokeExpr(v: VirtualInvokeExpr) {
                            if (analysisMode == AnalysisMode.MAY_P) outSet.add(leftOp)
                        }
                    })
                } catch (e: Exception) {
                }
            }

            override fun caseIdentityStmt(stmt: IdentityStmt) {
                val leftOp = stmt.leftOp as Local
                if (analysisMode == AnalysisMode.MAY_P) if (stmt.rightOp !is ThisRef) outSet.add(leftOp)
            }
        })
    }
}