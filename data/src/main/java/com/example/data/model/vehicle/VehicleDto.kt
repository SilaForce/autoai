package com.example.data.model.vehicle

import com.google.firebase.firestore.PropertyName

data class VehicleDto(
    val id: String = "",
    val userId: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val fuelType: String = "",
    val mileage: Int? = null,
    val licensePlate: String? = null,
    // Kotlin booleans named `isXxx` compile to a Java getter named `isXxx()`, which
    // Firestore's ClassMapper interprets as the Java-bean property "active" (strips the
    // "is" prefix). The @PropertyName annotations force Firestore to use the literal
    // field name "isActive" for both reads and writes, keeping it consistent with the
    // field name used in batch.update() calls in the repository.
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = false,
    val photoBase64: String? = null
)
