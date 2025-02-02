package net.deanly.structlayout.dispatcher;

import net.deanly.structlayout.annotation.StructTypeSelector;

import java.lang.reflect.InvocationTargetException;

public class StructTypeResolver {


    /**
     * Resolves the concrete class for a given base type and data.
     *
     * @param <F>       The resolved class type.
     * @param <T>       The base interface or class type.
     * @param data      The byte array containing the information needed to determine the class.
     * @param baseType  The base type annotated with @StructTypeSelector.
     * @param startOffset The starting position within the byte array to begin processing.
     * @return The resolved concrete class.
     * @throws NoSuchMethodException if the dispatcher constructor is missing.
     * @throws InvocationTargetException if the dispatcher instantiation throws an exception.
     * @throws InstantiationException if the dispatcher instantiation fails.
     * @throws IllegalAccessException if the dispatcher constructor is not accessible.
     */
    @SuppressWarnings("unchecked")
    public static <F extends T, T> Class<F> resolveClass(byte[] data, Class<T> baseType, int startOffset) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        StructTypeSelector selector = baseType.getAnnotation(StructTypeSelector.class);
        if (selector == null) {
            throw new IllegalArgumentException("Missing @StructTypeSelector on " + baseType.getName());
        }

        StructTypeDispatcher dispatcher = selector.dispatcher().getDeclaredConstructor().newInstance();

        // 바이트 데이터로부터 구체 클래스 결정
        Class<?> concreteClass = dispatcher.dispatch(data, startOffset);
        if (concreteClass == null) {
            throw new IllegalArgumentException("No matching class found for data: " + data[0]);
        }

        // `concreteClass`가 `T`를 상속 또는 구현하는지 확인
        if (!baseType.isAssignableFrom(concreteClass)) {
            throw new IllegalArgumentException(
                    "Resolved class " + concreteClass.getName() + " does not extend or implement " + baseType.getName()
            );
        }

        return (Class<F>) concreteClass;
    }

    /**
     * Retrieves the no-data span for the given base type using the annotated dispatcher.
     *
     * @param baseType The base type annotated with @StructTypeSelector.
     * @return The no-data span as returned by the dispatcher.
     * @throws NoSuchMethodException if the dispatcher constructor is missing.
     * @throws InvocationTargetException if the dispatcher instantiation throws an exception.
     * @throws InstantiationException if the dispatcher instantiation fails.
     * @throws IllegalAccessException if the dispatcher constructor is not accessible.
     */
    public static int resolveNoDataSpan(Class<?> baseType)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        StructTypeSelector selector = baseType.getAnnotation(StructTypeSelector.class);
        if (selector == null) {
            throw new IllegalArgumentException("Missing @StructTypeSelector on " + baseType.getName());
        }

        StructTypeDispatcher dispatcher = selector.dispatcher().getDeclaredConstructor().newInstance();

        // Retrieve the no-data span value from the dispatcher
        return dispatcher.getNoDataSpan();
    }
}
