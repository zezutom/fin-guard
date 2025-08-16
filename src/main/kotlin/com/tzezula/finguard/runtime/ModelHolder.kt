package com.tzezula.finguard.runtime

import com.tzezula.finguard.model.Model
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

@Component
class ModelHolder(initialModel: Model = Model.empty()) {
    private val ref: AtomicReference<Model> = AtomicReference(initialModel)

    fun current(): Model = ref.get()

    fun swap(next: Model): Unit = ref.set(next)
}
