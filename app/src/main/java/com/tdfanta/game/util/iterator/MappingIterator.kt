package com.tdfanta.game.util.iterator

class MappingIterator<F, T>(
    private val mOriginal: StreamIterator<F>,
    private val mMapper: Function<in F, out T>,
) : StreamIterator<T>() {
    override fun close() {
        mOriginal.close()
    }

    override fun hasNext(): Boolean = mOriginal.hasNext()

    override fun next(): T = mMapper.apply(mOriginal.next())
}
