package xyz.neruxov.nocode.backend.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class NotificationService {

    init {
        val configuration = ClassPathResource("firebase.json").inputStream
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(configuration))
            .build()

        FirebaseApp.initializeApp(options)
    }

    final fun sendNotification(
        deviceId: String,
        title: String,
        text: String
    ) {
        val message = Message.builder()
            .putData("title", title)
            .putData("text", text)
            .setToken(deviceId)
            .build()

        val response = FirebaseMessaging.getInstance().send(message)
        println("Successfully sent message: $response")
    }

}