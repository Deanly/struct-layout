# StructLayout
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Java](https://img.shields.io/badge/Pure-Java-orange)](https://www.java.com/)
[![Maven Central](https://img.shields.io/maven-central/v/net.deanly/struct-layout.svg)](https://central.sonatype.com/artifact/net.deanly/struct-layout)

StructLayout is a Java library designed to simplify working with **binary data** using structured layouts. It allows you to easily encode/decode complex data structures into/from byte arrays by just defining a `Struct` class with simple annotations.

With StructLayout, managing structured binary data—common in file processing, network protocols, and serialization—becomes straightforward and intuitive.

---

## Features

- **Easy-to-Use Annotations**:
  Use annotations like `@StructField` to define the binary structure of your data with minimal boilerplate code.
- **Support for Common Data Types**:
  Easily handle primitive data types (`uint8`, `int32`, etc.), strings, arrays, and even nested structures.
- **Efficient Encoding/Decoding**:
  Transform objects to binary data and vice versa in just one line of code using `StructLayout.encode()` and `StructLayout.decode()`.
- **Debugging Support**:
  Visualize and debug complex binary data with built-in utilities like `StructLayout.debug()`.

---

## Installation

To use StructLayout, you need **Java 17** or higher. StructLayout can be used from Maven Central as shown below.

### Adding GitHub Maven Repository

To integrate StructLayout into your project, you need to add the following repository to your **Maven** or **Gradle** configuration.

#### For Maven:
And include the dependency in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>net.deanly</groupId>
        <artifactId>struct-layout</artifactId>
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

#### For Gradle:
Add the GitHub repository to your `build.gradle` file:

```gradle
dependencies {
    implementation 'net.deanly:struct-layout:0.1.1'
}
```

---

## Usage

### Define a Struct Class

Define a class to represent the structure of your binary data. Use StructLayout annotations like `@StructField` for fields and `@SequenceField` for collections.

```java
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.annotation.SequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.CustomLayoutField;
import net.deanly.structlayout.type.DataType;
import net.deanly.structlayout.Layout;

// Define your Struct class
class SimpleStruct {
    @StructField(order = 1, dataType = DataType.INT32_LE)
    private int int32Value;

    @StructField(order = 2, dataType = DataType.INT32_BE)
    private int int32BeValue;

    @StructField(order = 3, dataType = DataType.FLOAT32_LE)
    private float floatValue;

    @StructField(order = 4, dataType = DataType.STRING_C)
    private String stringValue;

    @SequenceField(order = 5, elementType = DataType.FLOAT32_LE)
    private float[] floatArray;

    @SequenceField(order = 6, elementType = DataType.FLOAT64_BE)
    private List<Double> doubleList;

    @StructObjectField(order = 7)
    private CustomStruct customStruct;

    // Getter and Setter...
}

class CustomStruct {
    @StructField(order = 1, dataType = DataType.INT32_LE)
    private long id; // Although it's INT32_LE in bytes, it is represented as a long type in Java.

    @CustomLayoutField(order = 2, layout = KeyLayout.class)
    private String key;

    public CustomStruct() {
    }
    public CustomStruct(long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getter and Setter...
}

class KeyLayout extends Layout<String> {
  private static final int KEY_LENGTH = 32; // 32 bytes
  public KeyLayout() {
    super(KEY_LENGTH);
  }

  @Override
  public byte[] encode(String value) {
    return value.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String decode(byte[] buffer, int offset) {
    return new String(buffer, offset, KEY_LENGTH, StandardCharsets.UTF_8);
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
        struct.setInt32Value(42);
        struct.setInt32BeValue(42);
        struct.setFloatValue(123.45f);
        struct.setStringValue("Hello, StructObject!");
        struct.setFloatArray(new float[]{3.14f, 1.59f});
        struct.setDoubleList(List.of(1.23, 4.56));
        struct.setCustomStruct(new CustomStruct(7L, "11111111111111111111111111111111"));

        // Encode to byte array
        byte[] serializedData = StructLayout.encode(struct);

        // Decode from byte array
        SimpleStruct decodedStruct = StructLayout.decode(serializedData, SimpleStruct.class);

        // Debug the byte array
        System.out.println("Debug Serialized Data:");
        StructLayout.debug(serializedData);

        // Output the decoded struct
        System.out.println("Decoded Struct:");
        System.out.println("Int32 Value (Little-Endian): " + struct.getInt32Value());
        System.out.println("Int32 Value (Big-Endian): " + struct.getInt32BeValue());
        System.out.println("Float Value: " + struct.getFloatValue());
        System.out.println("String Value: " + struct.getStringValue());
        System.out.print("Float Array: ");
        for (float f : struct.getFloatArray()) {
            System.out.print(f + " ");
        }
        System.out.println();
        System.out.print("Double List: ");
        for (double d : struct.getDoubleList()) {
            System.out.print(d + " ");
        }
        System.out.println();
        System.out.println("Custom Struct:");
        System.out.println("  ID: " + struct.getCustomStruct().getId());
        System.out.println("  Key: " + struct.getCustomStruct().getKey());

        // Debug the decoded struct
        System.out.println("Debug Decoded Struct:");
        StructLayout.debug(decodedStruct);
    }
}
```

Example Output:
```aiignore
Debug Serialized Data:
00000000: 2a 00 00 00 00 00 00 2a 66 e6 f6 42 48 65 6c 6c   *......*f..BHell
00000010: 6f 2c 20 53 74 72 75 63 74 4f 62 6a 65 63 74 21   o, StructObject!
00000020: 00 02 00 00 00 c3 f5 48 40 1f 85 cb 3f 02 00 00   .......H@...?...
00000030: 00 3f f3 ae 14 7a e1 47 ae 40 12 3d 70 a3 d7 0a   .?...z.G.@.=p...
00000040: 3d 07 00 00 00 31 31 31 31 31 31 31 31 31 31 31   =....11111111111
00000050: 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31   1111111111111111
00000060: 31 31 31 31 31                                    11111

Decoded Struct:
Int32 Value (Little-Endian): 42
Int32 Value (Big-Endian): 42
Float Value: 123.45
String Value: Hello, StructObject!
Float Array: 3.14 1.59 
Double List: 1.23 4.56 
Custom Struct:
  ID: 7
  Key: 11111111111111111111111111111111
  
Debug Decoded Struct:
00000000: 2a 00 00 00 00 00 00 2a 66 e6 f6 42 48 65 6c 6c   *......*f..BHell
00000010: 6f 2c 20 53 74 72 75 63 74 4f 62 6a 65 63 74 21   o, StructObject!
00000020: 00 02 00 00 00 c3 f5 48 40 1f 85 cb 3f 02 00 00   .......H@...?...
00000030: 00 3f f3 ae 14 7a e1 47 ae 40 12 3d 70 a3 d7 0a   .?...z.G.@.=p...
00000040: 3d 07 00 00 00 31 31 31 31 31 31 31 31 31 31 31   =....11111111111
00000050: 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31   1111111111111111
00000060: 31 31 31 31 31                                    11111
```
---

## License

StructLayout is licensed under the **MIT License**. For more details, refer to the [LICENSE](LICENSE) file.
