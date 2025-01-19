# StructLayout

StructLayout is a Java library designed to simplify working with **binary data** using structured layouts. It allows you to easily encode/decode complex data structures into/from byte arrays by just defining a `Struct` class with simple annotations.

With StructLayout, managing structured binary data—common in file processing, network protocols, and serialization—becomes straightforward and intuitive.

---

## Features

- **Easy-to-Use Annotations**: Automatically map fields in a class to binary representations via annotations.
- **Support for Common Data Types**: Handle `uint8`, `int8`, `int32`, `float32`, `string`, and more.
- **Efficient Encoding/Decoding**: Serialize/deserialize objects to/from byte arrays with minimal effort.
- **Debugging Support**: Print or inspect byte arrays and their structured data representations in a human-readable way.

---

## Installation

To use StructLayout, you need **Java 17** or higher. StructLayout is not yet available on Maven Central, so you’ll need to use our GitHub Maven repository.

### Adding GitHub Maven Repository

To integrate StructLayout into your project, you need to add the following repository to your **Maven** or **Gradle** configuration.

#### For Maven:
Add the GitHub repository in your `settings.xml` file:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/deanly/struct-layout</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

And include the dependency in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>net.deanly</groupId>
        <artifactId>structlayout</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

#### For Gradle:
Add the GitHub repository to your `build.gradle` file:

```gradle
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/deanly/struct-layout")
    }
}

dependencies {
    implementation 'net.deanly:structlayout:1.0.0'
}
```

---

## Usage

### Define a Struct Class

Define a class to represent the structure of your binary data. Use StructLayout annotations like `@StructField` for fields and `@SequenceField` for collections.

```java
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.type.DataType;

// Define your Struct class
public class SimpleStruct {
    @StructField(order = 1, dataType = DataType.INT32_LE)
    private int intValue;

    @StructField(order = 2, dataType = DataType.FLOAT32_LE)
    private float floatValue;

    @SequenceField(order = 3, elementType = DataType.BYTE)
    private byte[] byteArray;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }
}
```

---

### Encode and Decode Data

Use `StructLayout.encode` and `StructLayout.decode` to convert between your struct and byte arrays.

```java
import net.deanly.structlayout.StructLayout;

public class StructExample {
    public static void main(String[] args) {
        // Create and populate the struct
        SimpleStruct struct = new SimpleStruct();
        struct.setIntValue(42);
        struct.setFloatValue(3.14f);
        struct.setByteArray(new byte[] { 1, 2, 3 });

        // Encode to byte array
        byte[] serializedData = StructLayout.encode(struct);

        // Decode from byte array
        SimpleStruct decodedStruct = StructLayout.decode(serializedData, SimpleStruct.class);

        // Debug the byte array
        StructLayout.debug(serializedData);

        // Output the decoded struct
        System.out.println("Decoded Struct:");
        System.out.println("Int Value: " + decodedStruct.getIntValue());
        System.out.println("Float Value: " + decodedStruct.getFloatValue());
    }
}
```

Example Output:
```aiignore
Decoded Struct: 
Int Value: 42 
Float Value: 3.14
```
---

### Debugging Byte Data

Use `StructLayout.debug` to print your binary data in a human-readable format. It works with both byte arrays and structured objects:

```java
// Debugging a byte array
StructLayout.debug(serializedData);

// Debugging a struct
StructLayout.debug(decodedStruct);
```

#### Example Output:
```
[DEBUG] Bytes: 00000000: 2a 00 00 00 c3 f5 48 40 03 00 00 00 01 02 03 *.....H@.......
[DEBUG] Struct Representation: Int Value: 42 Float Value: 3.14 Byte Array: [1, 2, 3]
```

---

## Running Tests

You can run the tests to ensure everything works correctly. StructLayout uses JUnit for testing.

### Run Tests with Maven:
```bash
mvn test
```

### Run Tests with Gradle:
```bash
gradle test
```

---

## License

StructLayout is licensed under the **MIT License**. For more details, refer to the [LICENSE](LICENSE) file.

---

## Contact

If you have any questions or suggestions, please feel free to contact us at:  
**developer@deanly.net**

---

Start structuring your binary data with **StructLayout** today! 