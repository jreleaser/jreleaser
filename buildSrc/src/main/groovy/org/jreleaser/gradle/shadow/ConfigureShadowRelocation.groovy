package org.jreleaser.gradle.shadow

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile

class ConfigureShadowRelocation extends DefaultTask {
    @Input
    ShadowJar target

    @Input
    @Optional
    List<String> exclusions = []

    @Input
    String prefix = 'jreleaser.shadow'

    @InputFiles
    @Optional
    List<Configuration> getConfigurations() {
        return target.configurations
    }

    void exclude(String str) {
        exclusions << str
    }

    @TaskAction
    void configureRelocation() {
        def packages = [] as Set<String>
        configurations.each {
            configuration ->
                configuration.files.each { jar ->
                    JarFile jf = new JarFile(jar)
                    boolean excluded = false
                    for (String exclusion : exclusions) {
                        if (jar.name.contains(exclusion) || jar.name ==~ /${exclusion}/) {
                            excluded = true
                        }
                    }

                    if (!excluded) {
                        jf.entries().each {
                            entry ->
                                if (entry.name.endsWith(".class") && entry.name != "module-info.class") {
                                    String entryName = entry.name[0..entry.name.lastIndexOf('/') - 1].replaceAll('/', '.')
                                    def matcher = entryName =~ /META-INF\.versions\.\d+\..*?/
                                    if (matcher) {
                                        entryName =  entryName - matcher[0]
                                    }
                                    if (!entryName.contains('META-INF')) packages << entryName
                                }
                        }
                    }

                    jf.close()
                }
        }
        packages.sort().each {
            target.relocate(it, "${prefix}.${it}")
        }
    }

    static String taskName(Task task) {
        return "configureRelocation${task.name.capitalize()}"
    }
}
