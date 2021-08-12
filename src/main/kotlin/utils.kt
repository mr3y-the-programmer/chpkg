import org.jetbrains.annotations.TestOnly
import java.io.File

@TestOnly
internal fun String.isPackageName(): Boolean {
    val lastSeg = this.split('.').last()
    return this.matches(Regex("""([a-z0-9_]+\.?)+""")) && lastSeg.matches(Regex("""[a-z0-9_]+"""))
}

@TestOnly
internal fun String.isModuleName(): Boolean {
    return this.matches(Regex("""include\(":[\w-]+"\)|include ':[\w-]+'|include ":[\w-]+""""))
}

internal fun String.trimLastDot() = this.removeSuffix(".")

internal fun String.trimInclude() = when {
    this.startsWith("include(") -> {
        this.removePrefix("include(\":").removeSuffix("\")")
    }
    this.startsWith("include '") -> {
        this.removePrefix("include ':").removeSuffix("'")
    }
    this.startsWith("include \"") -> {
        this.removePrefix("include \":").removeSuffix("\"")
    }
    else -> this
}

internal fun String.containsMoreThanOnce(other: String) = this.split(other).size > 2

internal fun File.replace(oldVal: String, newVal: String) {
    // TODO: should we write/read using okio?
    writeText(readText().replace(oldVal, newVal))
}

internal fun File.replace(regex: Regex, newVal: String) {
    writeText(readText().replace(regex, newVal))
}