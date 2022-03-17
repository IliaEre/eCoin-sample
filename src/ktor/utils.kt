package ktor

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.getParam(param: String) =
    this.call.receiveParameters()[param] ?: error("$param not found!")