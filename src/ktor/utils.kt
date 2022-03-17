package ktor

import io.ktor.application.*
import io.ktor.util.pipeline.*

fun PipelineContext<*, ApplicationCall>.getParam(param: String) =
    this.call.parameters[param] ?: error("$param not found!")
