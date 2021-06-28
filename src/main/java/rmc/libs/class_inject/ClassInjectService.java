package rmc.libs.class_inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;

import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;

/**
 * Developed by RMC Team, 2021
 * @author KR33PY
 */
public class ClassInjectService implements ILaunchPluginService {

    private static final Logger LOGGER = LogManager.getLogger();
    private EnumSet<Phase> phaseSet = EnumSet.of(Phase.BEFORE);

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, final boolean isEmpty) {
        return this.phaseSet;
    }

    @Override
    public boolean processClass(final Phase phase, ClassNode classNode, final Type classType) {
        Path dir = Paths.get("classboot");
        if (Files.isDirectory(dir)) {
            try {
                LOGGER.info("Class Inject is starting!");
                Field loaderField = TransformingClassLoader.class.getDeclaredField("delegatedClassLoader");
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                loaderField.setAccessible(true);
                addUrlMethod.setAccessible(true);
                Object loader = loaderField.get(Thread.currentThread().getContextClassLoader());
                List<Path> jarList = Files.list(dir).filter(jar -> jar.toString().endsWith(".jar")).collect(Collectors.toList());
                for (Path jar : jarList) {
                    LOGGER.info("Adding new jar file: " + jar.getFileName());
                    addUrlMethod.invoke(loader, jar.toUri().toURL());
                    try (ZipInputStream zipInput = new ZipInputStream(Files.newInputStream(jar))) {
                        ZipEntry zipEntry;
                        while ((zipEntry = zipInput.getNextEntry()) != null) {
                            String cfg = zipEntry.getName();
                            if (cfg.endsWith(".mixins.json")
                             || (cfg.startsWith("mixins.")
                              && cfg.endsWith(".json")
                              && !cfg.endsWith(".refmap.json"))) {
                                LOGGER.info("Adding new mixin cfg: " + cfg);
                                Mixins.addConfiguration(cfg);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Class Inject failed to start, exiting :(");
                ex.printStackTrace();
                System.exit(1);
            }
        }
        this.phaseSet = EnumSet.noneOf(Phase.class);
        return false;
    }

}
