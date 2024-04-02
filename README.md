
# YourLibraryName

RosterParser is a Kotlin (and/or Java) library designed to simplify the parsing of flight crew rosters from various formats, including CSV, PDF, and TXT, into Java objects. It aims to provide an easy-to-use interface for developers working with crew roster data, making the extraction of information seamless and efficient.

## Features

- Parse flight crew rosters into Java objects.
- Supported formats: CSV, PDF, TXT (more formats might be supported in future releases - let me know what you need).
- Easy to integrate into existing Kotlin/Java projects.

## Getting Started

To use RosterParser in your project, include the following in your project's build file:

### Gradle (Kotlin DSL)
```Kotlin
repositories {
    maven {
        url = uri("https://joozd.nl/nexus/repository/maven-releases/")
    }
}
dependencies {
    implementation("nl.joozd:practicelibrary:0.0.2")
}
```

## Usage

Basic usage example:

```kotlin
// Example of how to use the library to parse a CSV file
val csvTestFile = File(this::class.java.classLoader.getResource("joozdlogv5test.csv")!!.toURI())
val mimeType = "text/csv"
val parsedRoster = csvTestFile.inputStream().use {
    RosterParser.getRoster(it, mimeType)
}
// Work with the parsed roster object
```

Replace `"path/to/your/roster.csv"` with the actual path to your roster file and `Format.CSV` with the appropriate format of your roster file.

## License: AGPL

RosterParser is distributed under the Affero General Public License (AGPL). This ensures that all modifications and derived works are also licensed under the AGPL. By using this library, you are agreeing to comply with the terms of this license.

The reason we chose this licence is that one of the modules used to parse PDF data requires us to.
If you need a less restricting license, and can do without the PDF functionality, let me know and we can work something out.

The source code of RosterParser, including any modifications or derived works, is available on [GitHub](https://github.com/Joozd/rosterparser/). This fulfills the AGPL requirement that all software interacting with users over a network provide access to its source code.

For more details on our license, see the [LICENSE](LICENSE) file included with the source code.

## Contributing

We welcome contributions to RosterParser! If you're interested in helping:

1. Fork the repository.
2. Create a new branch for your feature or fix.
3. Commit your changes with clear, descriptive messages.
4. Push your branch and submit a pull request.

Please note that by contributing to this project, you agree to have your work distributed under the same AGPL license.

## Contact

For questions, suggestions, or concerns, please open an issue on GitHub or contact us directly at joozd@joozd.nl.

Thank you for considering RosterParser for your flight crew roster parsing needs!
