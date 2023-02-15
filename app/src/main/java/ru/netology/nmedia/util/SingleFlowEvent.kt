package ru.netology.nmedia.util

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector

private var pending = false

@FlowPreview
class SingleFlowEvent<T> : AbstractFlow<T>() {
    override suspend fun collectSafely(collector: FlowCollector<T>) {
        if (pending) {
            pending = false
            super.collect(SingleFlowCollector())
        }
    }
}

class SingleFlowCollector<T> : FlowCollector<T> {
    override suspend fun emit(value: T) {
        pending = true
//        super.emit(value)
    }
}