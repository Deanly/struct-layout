# StructLayout
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Java](https://img.shields.io/badge/Pure-Java-orange)](https://www.java.com/)
[![Maven Central](https://img.shields.io/maven-central/v/net.deanly/struct-layout.svg?color=008080)](https://central.sonatype.com/artifact/net.deanly/struct-layout)
![Runtime Dependencies](https://img.shields.io/badge/runtime%20dependencies-none-brightgreen.svg)


StructLayout is a **flexible binary serialization library for Java**,  
designed to simplify encoding and decoding of structured binary data.  
With an annotation-based approach, StructLayout allows you to define complex binary structures effortlessly.  
It is ideal for use cases such as **file processing, network protocols, blockchain data (e.g., Solana Borsh), and custom serialization formats**.

### 🚀 Key Goals
- **Borsh and Beyond**: Supports Borsh serialization along with other binary serialization formats.
- **TLV (Type-Length-Value) Compatibility**: Provides TLV support for seamless integration with external systems.
- **Fixed-Length Array Support**: Enables defining structured binary data with fixed-size arrays.
- **Improved Optional Field Handling**: Enhances flexibility in handling nullable fields for better usability.

StructLayout is designed to be **efficient, extensible, and easy to integrate** into any system requiring structured binary data serialization.

🔔 **Versioning Notice**  
StructLayout is under active development and may undergo structural changes until version **1.0.0** is released.  
**Minor versions will be incremented for any structural modifications**, ensuring clear versioning consistency.  
Once all Key Goals are met, StructLayout will be officially released as **version 1.0.0**.

---

## Features

- **Easy-to-Use Annotations**:
  Use annotations like `@StructField` to define the binary structure of your data with minimal boilerplate code.
- **Support for Common Data Types**:
  Easily handle primitive data types (`uint8`, `int32`, etc.), strings, arrays, and even nested structures.
- **Efficient Encoding/Decoding**:
  Transform objects to binary data and vice versa in just one line of code using `StructLayout.encode()` and `StructLayout.decode()`.
- **Debugging Support**:
  Visualize and debug complex binary data in a human-readable format using the `StructLayout.debug()` utility.

---

## Installation

To use StructLayout, you need **Java 17** or higher. StructLayout can be used from Maven Central as shown below.

### Adding the Dependency

To integrate StructLayout into your project, you need to add the following repository to your **Maven** or **Gradle** configuration.

#### For Maven:
And include the dependency in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>net.deanly</groupId>
        <artifactId>struct-layout</artifactId>
        <version>0.3.5</version>
    </dependency>
</dependencies>
```

#### For Gradle:
Add the GitHub repository to your `build.gradle` file:

```gradle
dependencies {
    implementation 'net.deanly:struct-layout:0.3.5'
}
```

---

## Usage

### Define a Struct Class

Define a Java class to represent the layout of structured binary data. Add `StructLayout` annotations to its fields to specify the order and data type of each field.

```java
import net.deanly.structlayout.annotation.StructSequenceField;
import net.deanly.structlayout.annotation.StructObjectField;
import net.deanly.structlayout.annotation.StructField;
import net.deanly.structlayout.type.Field;

// Define your Struct class
class SimpleStruct {
    @StructField(order = 1, type = Int32LEField.class)
    private int int32Value;

    @StructField(order = 2, type = Int32BEField.class)
    private int int32BeValue;

    @StructField(order = 3, type = Float32LEField.class)
    private float floatValue;

    @StructField(order = 4, type = StringCField.class)
    private String stringValue;

    @StructSequenceField(order = 5, elementType = Float32LEField.class, lengthType = Int32LEField.class)
    private float[] floatArray;

    @StructSequenceField(order = 6, elementType = Float64BEField.class)
    private List<Double> doubleList;

    @StructObjectField(order = 7)
    private CustomStruct customStruct;

    @StructSequenceObjectField(order = 8, lengthType = Int16LEField.class)
    private List<CustomStruct> customStructList;

    // Getter and Setter...
}

class CustomStruct {
    @StructField(order = 1, type = Int32LEField.class)
    private long id; // Although it's INT32_LE in bytes, it is represented as a long type in Java.

    @StructField(order = 2, type = KeyField.class)
    private Key key;

    public CustomStruct() { }
    public CustomStruct(long id, Key name) {
        this.id = id;
        this.name = name;
    }
    
    // Getter and Setter...
}

public static class KeyField extends FieldBase<Key> {
    private static final int KEY_LENGTH = 32; // 32 bytes
    public KeyField() {
        super(KEY_LENGTH);
    }

    @Override
    public byte[] encode(Key value) {
        byte[] byteValue = value.getKey().getBytes(StandardCharsets.UTF_8);
        if (byteValue.length != KEY_LENGTH) {
            throw new RuntimeException(String.format("Invalid KeyField length: expected %d bytes but was %d bytes.", KEY_LENGTH, byteValue.length));
        }
        return byteValue;
    }

    @Override
    public Key decode(byte[] buffer, int offset) {
        if (buffer == null || buffer.length < offset + KEY_LENGTH) {
            throw new RuntimeException(String.format("Invalid buffer length for KeyField decoding. Expected at least %d bytes from offset %d.", KEY_LENGTH, offset));
        }
        return new Key(new String(buffer, offset, KEY_LENGTH, StandardCharsets.UTF_8));
    }
}

public static class Key {
    private final String key;
    public Key(String key) { this.key = key; }
    
    public String getKey() {
        return key;
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
        struct.setCustomStruct(new CustomStruct(7L, new Key("11111111111111111111111111111111")));
        struct.setCustomStructList(List.of(
                new CustomStruct(10000L, new Key("11111111111111111111111111111111")),
                new CustomStruct(80000L, new Key("11111111111111111111111111111111"))
        ));

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
00000020: 00 02 00 00 00 c3 f5 48 40 1f 85 cb 3f 02 3f f3   .......H@...?.?.
00000030: ae 14 7a e1 47 ae 40 12 3d 70 a3 d7 0a 3d 07 00   ..z.G.@.=p...=..
00000040: 00 00 31 31 31 31 31 31 31 31 31 31 31 31 31 31   ..11111111111111
00000050: 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31   1111111111111111
00000060: 31 31 02 00 10 27 00 00 31 31 31 31 31 31 31 31   11...'..11111111
00000070: 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31   1111111111111111
00000080: 31 31 31 31 31 31 31 31 80 38 01 00 31 31 31 31   11111111.8..1111
00000090: 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31   1111111111111111
000000a0: 31 31 31 31 31 31 31 31 31 31 31 31               111111111111

Decoded Struct:
Int32 Value (Little-Endian): 42
Int32 Value (Big-Endian): 42
Float Value: 123.45
String Value: Hello, StructObject!
Float Array: 3.14 1.59 
Double List: 1.23 4.56 
Custom Struct:
  ID: 7
  Key: Key(key=11111111111111111111111111111111)

Debug Decoded Struct:
Order      Field            Offset  Bytes (HEX)
=====================================================
1          int32Value       0000000 2A 00 00 00
2          int32BeValue     0000004 00 00 00 2A
3          floatValue       0000008 66 E6 F6 42
4          stringValue      0000012 48 65 6C 6C 6F 2C 20 53 74 72 75 63 74 4F 62 6A 65 63 74 21 00
5[].length floatArray       0000033 02 00 00 00
5[0]       floatArray       0000037 C3 F5 48 40
5[1]       floatArray       0000041 1F 85 CB 3F
6[].length doubleList       0000045 02
6[0]       doubleList       0000046 3F F3 AE 14 7A E1 47 AE
6[1]       doubleList       0000054 40 12 3D 70 A3 D7 0A 3D
7-1        id               0000062 07 00 00 00
7-2        key              0000066 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31
8[].length customStructList 0000098 02 00
8[0]-1     id               0000100 10 27 00 00
8[0]-2     key              0000104 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31
8[1]-1     id               0000136 80 38 01 00
8[1]-2     key              0000140 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31
=====================================================
Total Bytes: 172


```
---

## License

StructLayout is licensed under the **MIT License**. For more details, refer to the [LICENSE](LICENSE) file.
