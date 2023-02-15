package ru.netology.nmedia.util

import androidx.lifecycle.*

class SingleLiveEvent<T>(value: T) : MutableLiveData<T>(value) {
    // Свойство pending отображает факт обработки события
    private var pending = false

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        // Если нет активных подписчиков, то показать ошибку
        require (!hasActiveObservers()) {
            error("Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner) {
            // Если событие еще ни разу не обрабатывалось,
            // тогда передаем его на обработку
            if (pending) {
                pending = false
                observer.onChanged(it)
            }
        }
    }

    override fun setValue(value: T?) {
        // При изменении контролируемого значения фиксируем,
        // что произошедшее событие еще не обработано
        pending = true
        super.setValue(value)
    }
}