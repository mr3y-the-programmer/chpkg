import org.jetbrains.annotations.TestOnly

@TestOnly
internal fun String.isPackageName(): Boolean {
    val lastSeg = this.split('.').last()
    return this.matches(Regex("""([a-z0-9_]+\.?)+""")) && lastSeg.matches(Regex("""[a-z0-9_]+"""))
}

@TestOnly
internal fun String.isModuleName(): Boolean {
    return this.matches(Regex("""include\(":[\w-]+"\)|include ':[\w-]+'|include ":[\w-]+""""))
}

internal fun String.trimLastDot() = if (this.endsWith(".")) this.removeSuffix(".") else this

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