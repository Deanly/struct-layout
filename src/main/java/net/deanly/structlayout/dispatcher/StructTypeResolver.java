package net.deanly.structlayout.dispatcher;

import net.deanly.structlayout.annotation.StructTypeSelector;

import java.lang.reflect.InvocationTargetException;

public class StructTypeResolver {

    @SuppressWarnings("unchecked")
    public static <F extends T, T> Class<F> resolve(byte[] data, Class<T> baseType) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        StructTypeSelector selector = baseType.getAnnotation(StructTypeSelector.class);
        if (selector == null) {
            throw new IllegalArgumentException("Missing @StructTypeSelector on " + baseType.getName());
        }

        StructTypeDispatcher dispatcher = selector.dispatcher().getDeclaredConstructor().newInstance();

        // 바이트 데이터로부터 구체 클래스 결정
        Class<?> concreteClass = dispatcher.dispatch(data);
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
}
