package dev.lattice.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.jar.Attributes
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Merges the four loader jars into one universal jar.
 *
 * <p>The input file collection is sorted by file name before merging. This is
 * intentional: the 1.21.11 shared entrypoint classes (classfile 65) must be
 * written before the 26.3 copies (classfile 69) so the single jar remains
 * loadable on Java 21. Duplicate non-lang entries keep the first copy; lang
 * JSON files are merged with later versions overriding duplicate keys.</p>
 */
abstract class MergeUniversalJarTask extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract ConfigurableFileCollection getInputJars()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getMetadataDir()

    @Input
    abstract MapProperty<String, String> getManifestAttributes()

    @OutputFile
    abstract RegularFileProperty getArchiveFile()

    @TaskAction
    void merge() {
        def target = archiveFile.get().asFile
        target.parentFile.mkdirs()
        def temp = File.createTempFile(target.name, '.tmp', target.parentFile)
        def written = new LinkedHashSet<String>()
        def langByFile = new TreeMap<String, Map<String, Object>>()

        try {
            new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temp))).withCloseable { out ->
                def manifest = new Manifest()
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, '1.0')
                manifestAttributes.get().each { key, value ->
                    manifest.getMainAttributes().putValue(key, value)
                }
                def manifestEntry = new ZipEntry('META-INF/MANIFEST.MF')
                manifestEntry.time = 0L
                out.putNextEntry(manifestEntry)
                manifest.write(out)
                out.closeEntry()
                written << 'META-INF/MANIFEST.MF'

                inputJars.files.sort { it.name }.each { jarFile ->
                    new ZipFile(jarFile).withCloseable { zip ->
                        zip.entries().toList().each { entry ->
                            if (entry.directory) {
                                return
                            }
                            def name = entry.name
                            if (name == 'META-INF/MANIFEST.MF') {
                                return
                            }
                            if (name == 'fabric.mod.json' || name == 'META-INF/neoforge.mods.toml') {
                                return
                            }
                            if (name.startsWith('META-INF/')
                                    && (name.endsWith('.SF') || name.endsWith('.DSA') || name.endsWith('.RSA'))) {
                                return
                            }
                            if (name == '.gitkeep' || name.endsWith('/.gitkeep')) {
                                return
                            }
                            if (name.startsWith('assets/lattice/lang/') && name.endsWith('.json')) {
                                def fileName = name.substring('assets/lattice/lang/'.length())
                                if (!fileName.contains('/')) {
                                    def parsed = new JsonSlurper().parseText(
                                            zip.getInputStream(entry).getText('UTF-8'))
                                    if (parsed instanceof Map) {
                                        langByFile.computeIfAbsent(fileName) {
                                            new LinkedHashMap<String, Object>()
                                        }.putAll(parsed as Map<String, Object>)
                                    }
                                }
                                return
                            }
                            if (!written.add(name)) {
                                return
                            }
                            def outEntry = new ZipEntry(name)
                            outEntry.time = 0L
                            out.putNextEntry(outEntry)
                            zip.getInputStream(entry).withCloseable { it.transferTo(out) }
                            out.closeEntry()
                        }
                    }
                }

                langByFile.each { fileName, values ->
                    def name = "assets/lattice/lang/${fileName}"
                    if (!written.add(name)) {
                        return
                    }
                    def outEntry = new ZipEntry(name)
                    outEntry.time = 0L
                    out.putNextEntry(outEntry)
                    out.write((JsonOutput.prettyPrint(JsonOutput.toJson(values)) + '\n').getBytes('UTF-8'))
                    out.closeEntry()
                }

                def metadataRoot = metadataDir.get().asFile
                if (metadataRoot.isDirectory()) {
                    metadataRoot.eachFileRecurse { file ->
                        if (!file.isFile()) {
                            return
                        }
                        def relative = metadataRoot.toPath()
                                .relativize(file.toPath())
                                .toString()
                                .replace('\\', '/')
                        if (!written.add(relative)) {
                            return
                        }
                        def outEntry = new ZipEntry(relative)
                        outEntry.time = 0L
                        out.putNextEntry(outEntry)
                        file.withInputStream { it.transferTo(out) }
                        out.closeEntry()
                    }
                }
            }

            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } finally {
            if (temp.exists()) {
                temp.delete()
            }
        }
    }
}
