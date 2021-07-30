import org.jetbrains.annotations.TestOnly

@TestOnly
internal fun String.isPackageName(): Boolean {
    val lastSeg = this.split('.').last()
    return this.matches(Regex("""([a-z0-9_]+\.?)+""")) && lastSeg.matches(Regex("""[a-z0-9_]+"""))
}

internal fun String.trimLastDot() = if (this.endsWith(".")) this.removeSuffix(".") else this