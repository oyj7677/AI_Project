package com.mystarnow.shared.core.model

sealed interface ViewEvent {
    data class Message(val text: String) : ViewEvent
}
