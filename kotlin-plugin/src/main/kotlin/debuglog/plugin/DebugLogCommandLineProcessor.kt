package debuglog.plugin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class) // don't forget!
class DebugLogCommandLineProcessor : CommandLineProcessor {
  companion object {
    val PLUGIN_ID: String = "debuglog"
    val OPTION_ENABLED = CliOption(
            optionName = "enabled", valueDescription = "<true|false>",
            description = "whether to enable the debuglog plugin or not"
    )
    val OPTION_ANNOTATION = CliOption(
            optionName = "debugLogAnnotation", valueDescription = "<fqname>",
            description = "fully qualified name of the annotation(s) to use as debug-log",
            required = true, allowMultipleOccurrences = true
    )
  }

  /**
   * Just needs to be consistent with the key for DebugLogGradleSubplugin#getCompilerPluginId
   */
  override val pluginId: String = PLUGIN_ID

  /**
   * Should match up with the options we return from our DebugLogGradleSubplugin.
   * Should also have matching when branches for each name in the [processOption] function below
   */
  override val pluginOptions: Collection<CliOption> = listOf(OPTION_ENABLED, OPTION_ANNOTATION)

  override fun processOption(
          option: AbstractCliOption,
          value: String,
          configuration: CompilerConfiguration
  ) = when (option) {
    OPTION_ENABLED -> configuration.put(ConfigurationKeys.KEY_ENABLED, value.toBoolean())
    OPTION_ANNOTATION -> configuration.appendList(ConfigurationKeys.KEY_ANNOTATIONS, value)
    else -> error("Unexpected config option ${option.optionName}")
  }
}

public object ConfigurationKeys {
  val KEY_ENABLED = CompilerConfigurationKey.create<Boolean>("whether the plugin is enabled")
  val KEY_ANNOTATIONS = CompilerConfigurationKey.create<List<String>>("our debuglog annotations")
}
