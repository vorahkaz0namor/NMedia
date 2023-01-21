package ru.netology.nmedia.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request

// Все, что находится внутри класса WorkerThread, будет выполняться в фоновом потоке
class WorkerThread : Thread() {
    // Чтобы связать WorkerThread и Fragment, используется Callback
    var resultCallback: (Bitmap) -> Unit = {}
    private val mainThreadHandler = Handler(Looper.getMainLooper()) {
        resultCallback(it.obj as Bitmap)
        true
    }
    private lateinit var handler: Handler
    private val client = OkHttpClient.Builder().build()

    override fun run() {
        // Чтобы у потока появился свой Looper, нужно вызвать функцию prepare()
        Looper.prepare()

        // В конструктор функции Handler входит callback, который вызовется,
        // когда будет передано сообщение (Message) через Message Queue
        handler = Handler(Looper.myLooper()!!) {
            // Каждое входящее событие сопровождается объектом типа Message,
            // в который можно записывать необходимые данные
            val url = it.obj as String

            try {
                val request = Request.Builder()
                    .url(url)
                    .build()
                val result = client.newCall(request).execute()
                Log.d("RESPONSE CODE:", result.code.toString())
                Log.d("RESPONSE URL REQUEST:", result.request.url.toString())
                // Преобразуем полученные данные в формат bitmap
                val bitmap = BitmapFactory
                    .decodeStream(requireNotNull(result.body).byteStream())
                if (bitmap != null) {
                    // Чтобы отправить полученные данные в основной поток, необходимо
                    // воспользоваться его handler'ом
                    // При этом аналогично создадим Message и отправим его в основной поток
                    val resultMessage = mainThreadHandler.obtainMessage()
                    resultMessage.obj = bitmap
                    mainThreadHandler.sendMessage(resultMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Если сообщение предназначалось для данного потока, тогда
            // можно вернуть true
            true
        }
        // Чтобы зациклить поток, нужно вызвать функцию loop(), но она будет работать
        // только для тех потоков, у которых есть объект Looper
        Looper.loop()
    }

    fun download(url: String) {
        // Объект Massage не создается вручную, а запрашивается из handler, поскольку
        // такие объекты хранятся в cache для экономии памяти
        val message = handler.obtainMessage()
        // Записываем данные, которые будем использовать в дальнейшем
        message.obj = url
        handler.sendMessage(message)
    }

}