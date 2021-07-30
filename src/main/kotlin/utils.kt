import org.jetbrains.annotations.TestOnly

@TestOnly
internal fun String.isPackageName(): Boolean {
    val pkg = if (this.endsWith(".")) this.removeSuffix(".") else this
    val lastSeg = pkg.split('.').last()
    return pkg.matches(Regex("""([a-z0-9_]+\.?)+""")) && lastSeg.matches(Regex("""[a-z0-9_]+"""))
}