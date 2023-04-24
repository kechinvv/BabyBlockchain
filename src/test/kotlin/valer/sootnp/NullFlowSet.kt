package valer.sootnp

import io.ktor.util.collections.*
import soot.Local
import soot.toolkits.scalar.AbstractBoundedFlowSet
import java.util.stream.Collectors


class NullFlowSet: AbstractBoundedFlowSet<Local?>() {
    private val nullLocals: MutableSet<Local> = ConcurrentSet()
    override fun clone(): NullFlowSet {
        val myClone = NullFlowSet()
        myClone.nullLocals.addAll(nullLocals)
        return myClone
    }

    override fun isEmpty(): Boolean {
        return nullLocals.isEmpty()
    }

    override fun size(): Int {
        return nullLocals.size
    }

    override fun add(obj: Local?) {
        nullLocals.add(obj!!)
    }

    override fun remove(obj: Local?) {
        if (nullLocals.contains(obj)) nullLocals.remove(obj)
    }

    override operator fun contains(obj: Local?): Boolean {
        return nullLocals.contains(obj)
    }

    override fun iterator(): MutableIterator<Local> {
        return nullLocals.iterator()
    }

    override fun toList(): List<Local> {
        return nullLocals.stream().collect(Collectors.toList())
    }
}