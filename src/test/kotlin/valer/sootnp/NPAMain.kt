package valer.sootnp

import soot.*
import soot.jimple.InvokeStmt
import soot.jimple.JimpleBody
import soot.toolkits.graph.TrapUnitGraph
import soot.toolkits.graph.UnitGraph
import java.io.File


object NPAMain {
    const val classpath = "C:\\Users\\valer\\IdeaProjects\\BabyBlockchain\\build\\libs\\BabyBlockchain-0.0.1.jar"

    private var sourceDirectory =
        "C:\\Users\\valer\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-U\\ch-0\\222.4345.14.plugins\\Kotlin\\kotlinc\\lib\\annotations-13.0.jar;" +
                "C:\\Program Files\\Java\\jdk1.8.0_261\\jre\\lib\\rt.jar;" +
                "C:\\Users\\valer\\AppData\\Local\\JetBrains\\Toolbox\\apps\\" +
                "IDEA-U\\ch-0\\222.4345.14.plugins\\Kotlin\\kotlinc\\lib\\kotlin-stdlib.jar;" +
                "C:\\Users\\valer\\AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-U\\ch-0\\222.4345.14.plugins\\Kotlin\\kotlinc\\lib\\kotlinx-coroutines-core-jvm.jar;" +
                classpath

    @JvmStatic
    fun main(args: Array<String>) {
        prepareAnalyze()
        val args = arrayOf(
            "-w",
            "-pp",
            "-allow-phantom-refs",
            "-process-dir",
            classpath
        )

        Scene.v().sootClassPath = sourceDirectory
        Main.main(args)
        G.reset()
    }

    fun prepareAnalyze() {
        if (!PackManager.v().hasPack("wjtp.ifds")) PackManager.v().getPack("wjtp")
            .add(Transform("wjtp.ifds", object : SceneTransformer() {
                override fun internalTransform(phaseName: String?, options: MutableMap<String, String>?) {
                    val classes = Scene.v().applicationClasses
                    File("src/test/kotlin/valer/sootnp/NPA.txt").printWriter().use { out ->
                        for (my_class in classes) {
                            for (sm in my_class.methods) {
                                out.println("Method: " + sm.signature)
                                val body = sm.retrieveActiveBody() as JimpleBody
                                val unitGraph: UnitGraph = TrapUnitGraph(body)
                                val npAnalyzers: MutableList<NullPointerAnalysis> = ArrayList()
                                npAnalyzers.add(NullPointerAnalysis(unitGraph, NullPointerAnalysis.AnalysisMode.MUST))
                                npAnalyzers.add(NullPointerAnalysis(unitGraph, NullPointerAnalysis.AnalysisMode.MAY_O))
                                npAnalyzers.add(NullPointerAnalysis(unitGraph, NullPointerAnalysis.AnalysisMode.MAY_P))
                                var c = 0
                                for (unit in body.units) {
                                    c++
                                    for (usedValueBox in unit.useBoxes) {
                                        if (usedValueBox.value is Local) {
                                            val usedLocal = usedValueBox.value as Local
                                            for (npa in npAnalyzers) {
                                                if (npa.getFlowBefore(unit)!!.contains(usedLocal)) {
                                                    out.println("    Line " + unit.javaSourceStartLineNumber + ": " + npa.analysisMode + " NullPointer usage of local " + usedLocal + " in unit " + unit)
                                                }
                                            }
                                        }
                                        if ((unit is InvokeStmt) && (usedValueBox.value.type == NullType.v())) {
                                            out.println("    Line " + unit.javaSourceStartLineNumber + ": MUST NullPointer usage in unit (" + c + ") " + unit)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }))
    }

}