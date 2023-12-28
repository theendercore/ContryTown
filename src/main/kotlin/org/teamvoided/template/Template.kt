package org.teamvoided.template


import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import org.teamvoided.template.commands.TestCommand


@Suppress("unused")
object Template {
    const val MODID = "template"

    @JvmField
    val LOGGER = LoggerFactory.getLogger(Template::class.simpleName)

    fun commonInit() {
        LOGGER.info("Hello from Common")
        TestCommand.init()
    }

    fun clientInit() {
        LOGGER.info("Hello from Client")
    }

    fun id(path: String) = Identifier(MODID, path)


}
