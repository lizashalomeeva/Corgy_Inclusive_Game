# Documentation Generation Guide

## Tool Overview
We use **Javadoc** for code documentation and **Swagger UI** for API documentation. 

## How to Generate Locally
1. Open Android Studio terminal.
2. Run the Gradle task: `./gradlew generateJavadoc`
3. The generated HTML files will be placed in `app/build/docs/javadoc/`.
4. Open `index.html` in your browser.

## Documentation Linting
We strictly use `-Xdoclint:all`. If your code is missing `@param`, `@return`, or has malformed tags, the build will fail.

*Read this in [Ukrainian](generate_docs-uk.md).*