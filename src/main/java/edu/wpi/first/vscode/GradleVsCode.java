package edu.wpi.first.vscode;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;

public class GradleVsCode implements Plugin<Project> {

  public void apply(Project project) {

    project.subprojects(subproject -> {
      subproject.apply(config -> {
        config.plugin(GradleVsCode.class);
      });
    });

    project.getPlugins().withType(NativeComponentPlugin.class, a -> {
      Project rootProject = project.getRootProject();
      VsCodeConfigurationExtension vsce = rootProject.getExtensions().findByType(VsCodeConfigurationExtension.class);
      if (vsce == null) {
        rootProject.getTasks().register("generateVsCodeConfig", VsCodeConfigurationTask.class, task -> {
          task.setGroup("VSCode");
          task.setDescription("Generate configuration file for VSCode");
          task.configFile.set(rootProject.getLayout().getBuildDirectory().file("vscodeconfig.json"));
        });

        rootProject.getTasks().register("generateCompileCommands", CompileCommandsConfigurationTask.class, task -> {
          task.setGroup("CompileCommands");
          task.setDescription("Generate compile_commands.json");
          task.configDirectory.set(rootProject.getLayout().getBuildDirectory().dir("compile_commands"));
        });
        rootProject.getExtensions().create("vscodeConfiguration", VsCodeConfigurationExtension.class);
        rootProject.getExtensions().getExtraProperties().set("VsCodeConfigurationTask", VsCodeConfigurationTask.class);
      }
      project.getPluginManager().apply(GradleVsCodeRules.class);
      try {
        project.getTasks().named("generateVsCodeConfig");
      } catch (UnknownTaskException ex) {
        project.getTasks().register("generateVsCodeConfig", Task.class, task -> {
          task.setGroup("VSCode");
          task.setDescription("Shim task to enable project creation");
        });
      }

      try {
        project.getTasks().named("generateCompileCommands");
      } catch (UnknownTaskException ex) {
        project.getTasks().register("generateCompileCommands", Task.class, task -> {
          task.setGroup("CompileCommands");
          task.setDescription("Shim task to enable project creation");
        });
      }
    });
  }
}
