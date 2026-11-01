package dev.lattice.gradle

import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Writes an IntelliJ Application run configuration into .idea/runConfigurations.
 *
 * <p>This task is attached to Loom's ideaSyncTask, so an IntelliJ Gradle sync
 * creates the Fabric Application entry automatically instead of requiring a
 * checked-in .run XML file.</p>
 */
abstract class GenerateIdeaApplicationRunTask extends DefaultTask {
    @Input
    abstract Property<String> getConfigName()

    @Input
    abstract Property<String> getModuleName()

    @Input
    abstract Property<String> getMainClass()

    @Input
    abstract Property<String> getVmParameters()

    @Input
    abstract Property<String> getWorkingDirectory()

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @TaskAction
    void generate() {
        def file = outputFile.get().asFile
        file.parentFile.mkdirs()
        def name = XmlUtil.escapeXml(configName.get())
        def module = XmlUtil.escapeXml(moduleName.get())
        def main = XmlUtil.escapeXml(mainClass.get())
        def vm = XmlUtil.escapeXml(vmParameters.get())
        def work = XmlUtil.escapeXml(workingDirectory.get())

        file.setText("""<component name="ProjectRunConfigurationManager">
  <configuration default="false" factoryName="Application" name="${name}" type="Application">
    <option name="MAIN_CLASS_NAME" value="${main}"/>
    <module name="${module}"/>
    <option name="PROGRAM_PARAMETERS" value=""/>
    <option name="VM_PARAMETERS" value="${vm}"/>
    <option name="WORKING_DIRECTORY" value="${work}"/>
    <method v="2">
      <option enabled="true" name="Make"/>
    </method>
    <envs>
    </envs>
    <shortenClasspath name="ARGS_FILE"/>
  <classpathModifications/></configuration>
</component>
""", 'UTF-8')
    }
}
