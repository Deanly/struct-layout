package net.deanly.structlayout.validation;

import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.DataType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("net.deanly.structlayout.annotation.StructField")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class StructFieldProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(StructField.class)) {
            StructField structField = element.getAnnotation(StructField.class);

            // 필드의 실제 타입
            TypeMirror fieldType = element.asType();

            // DataType에서 기대하는 타입 가져오기
            DataType dataType = structField.dataType();
            Class<?> expectedType = dataType.getFieldType();

            // 타입 이름 비교 (런타임 클래스와 어노테이션 클래스 매칭)
            if (!processingEnv.getTypeUtils().isSameType(
                    fieldType,
                    processingEnv.getElementUtils().getTypeElement(expectedType.getCanonicalName()).asType()
            )) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(
                                "Field '%s' has type '%s' but is annotated with DataType '%s' which expects type '%s'.",
                                element.getSimpleName(),
                                fieldType.toString(),
                                dataType.name(),
                                expectedType.getCanonicalName()
                        ),
                        element
                );
            }
        }
        return true;
    }
}
