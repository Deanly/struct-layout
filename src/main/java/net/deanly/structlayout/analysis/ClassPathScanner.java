package net.deanly.structlayout.analysis;

import lombok.extern.slf4j.Slf4j;
import net.deanly.structlayout.Layout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ClassPathScanner {

    public static List<Class<?>> findAllLayoutJavaClassPath() {
        List<Class<?>> layoutClasses = new ArrayList<>();
        try {
            // Classpath 가져오기
            String classpath = System.getProperty("java.class.path");
            String[] classpathElements = classpath.split(File.pathSeparator);

            for (String element : classpathElements) {
                File file = new File(element);
                layoutClasses.addAll(scanFileForClasses(file, ""));
            }

        } catch (Exception e) {
            log.error("Error while scanning classpath", e);
        }
        return layoutClasses;
    }

    private static List<Class<?>> scanFileForClasses(File file, String packageName) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        if (file.isDirectory()) {
            // 디렉토리일 경우 재귀적으로 탐색
            for (File entry : Objects.requireNonNull(file.listFiles())) {
                classes.addAll(scanFileForClasses(entry, packageName));
            }
        } else if (file.isFile() && file.getName().endsWith(".class")) {
            // .class 파일에서 클래스 로드
            String className = packageName + file.getName().replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className);
                if (Layout.class.isAssignableFrom(clazz) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                    classes.add(clazz);
                }
            } catch (ClassNotFoundException e) {
                log.warn("Class not found: " + className);
            }
        }
        return classes;
    }

    /**
     * Classpath에서 모든 클래스를 탐지하고, Layout 서브클래스를 반환
     */
    public static List<Class<?>> findAllLayoutClasses() {
        List<Class<?>> layoutClasses = new ArrayList<>();

        try {
            // JVM의 모든 클래스 로더에서 클래스 검색
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File file = new File(resource.getFile());
                layoutClasses.addAll(findClasses(file, ""));
            }
        } catch (IOException e) {
            log.error("Error while scanning classpath", e);
        }

        return layoutClasses;
    }

    /**
     * 특정 디렉터리에서 .class 파일을 탐색하고, Layout 서브클래스를 찾습니다.
     *
     * @param directory 탐색 대상 디렉터리
     * @param packageName 패키지 이름
     * @return 발견된 Layout 하위 클래스 목록
     */
    private static List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + file.getName() + "."));
            } else if (file.getName().endsWith(".class")) {
                try {
                    String className = packageName + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);

                    // Layout의 서브클래스인지 확인
                    if (Layout.class.isAssignableFrom(clazz) && !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                        classes.add(clazz);
                        log.debug("Found Layout subclass: {}", clazz.getName());
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Failed to load class: " + file.getName(), e);
                }
            }
        }
        return classes;
    }
}