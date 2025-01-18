package net.deanly.structlayout.factory;

import net.deanly.structlayout.Layout;
import net.deanly.structlayout.exception.LayoutInitializationException;
import net.deanly.structlayout.exception.NoDefaultConstructorException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class ClassFactory {

    public static <T> T createNoArgumentsInstance(Class<T> type) {
        try {
            // 비정적 내부 클래스 조건 확인
            if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) {
                throw new NoDefaultConstructorException(
                        "Class '" + type.getName() + "' is a non-static inner class and cannot be instantiated. Make it static or use another design."
                );
            }
            // 함수 내부 정의 클래스 조건 확인
            if (type.isLocalClass()) {
                throw new LayoutInitializationException(
                        "Class '" + type.getName() + "' is a local class defined inside a method. " +
                                "Local classes cannot be instantiated reflectively. Consider refactoring it to a static nested class or a top-level class."
                );
            }

            // 기본 생성자 호출
            return type.getDeclaredConstructor().newInstance();

        } catch (NoDefaultConstructorException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new LayoutInitializationException("The Layout class '"
                    + type.getName()
                    + "' must have a public no-arguments constructor.", e);
        } catch (IllegalAccessException e) {
            throw new LayoutInitializationException("Cannot access the constructor of Layout class: "
                    + type.getName(), e);
        } catch (InstantiationException e) {
            throw new LayoutInitializationException("Cannot instantiate Layout class: "
                    + type.getName(), e);
        } catch (InvocationTargetException e) {
            throw new LayoutInitializationException("Exception occurred while initializing Layout class: "
                    + type.getName() + ". Check the constructor logic.", e.getCause());
        } catch (Exception e) {
            throw new LayoutInitializationException("Failed to instantiate Layout class: "
                    + type.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Layout<T> createLayoutInstance(Class<? extends Layout<?>> layoutClass) {
        try {
            // 비정적(non-static) 내부 클래스인지 검증
            if (layoutClass.isMemberClass() && !Modifier.isStatic(layoutClass.getModifiers())) {
                throw new LayoutInitializationException("The Layout class '"
                        + layoutClass.getName()
                        + "' is a non-static inner class. Make it static or redesign it properly.");
            }
            // 함수 내부 정의 클래스 조건 확인
            if (layoutClass.isLocalClass()) {
                throw new LayoutInitializationException(
                        "Class '" + layoutClass.getName() + "' is a local class defined inside a method. " +
                                "Local classes cannot be instantiated reflectively. Consider refactoring it to a static nested class or a top-level class."
                );
            }

            // 생성자 가져오기
            Constructor<? extends Layout<?>>[] constructors =
                    (Constructor<? extends Layout<?>>[]) layoutClass.getDeclaredConstructors();

            boolean hasNoArgConstructor = false;
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    hasNoArgConstructor = true;
                    break;
                }
            }

            if (!hasNoArgConstructor) {
                throw new LayoutInitializationException("The Layout class '"
                        + layoutClass.getName()
                        + "' does not have a valid no-arguments constructor. " +
                        "If it is a non-static inner class, make it 'static' or define its constructor properly.");
            }

            var constructor = layoutClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Layout<T>) constructor.newInstance();

        } catch (LayoutInitializationException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new LayoutInitializationException("The Layout class '"
                    + layoutClass.getName()
                    + "' must have a public no-arguments constructor.", e);
        } catch (IllegalAccessException e) {
            throw new LayoutInitializationException("Cannot access the constructor of Layout class: "
                    + layoutClass.getName(), e);
        } catch (InstantiationException e) {
            throw new LayoutInitializationException("Cannot instantiate Layout class: "
                    + layoutClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new LayoutInitializationException("Exception occurred while initializing Layout class: "
                    + layoutClass.getName() + ". Check the constructor logic.", e.getCause());
        } catch (Exception e) {
            throw new LayoutInitializationException("Failed to instantiate Layout class: "
                    + layoutClass.getName(), e);
        }
    }
}
