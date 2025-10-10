# GeoCell Desktop Analyst

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-green.svg)
![PostGIS](https://img.shields.io/badge/PostGIS-3.3-lightgreen.svg)

A comprehensive desktop application for analyzing and exporting cellular network data to KMZ format, developed in Kotlin with JavaFX.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Data Models](#data-models)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

**GeoCell Desktop Analyst** is a specialized tool for telecommunications professionals that enables:

- **Processing** CSV files with cellular network cell data
- **Querying** PostgreSQL/PostGIS databases with infrastructure information
- **Generating** organized KMZ files for visualization in Google Earth
- **Analyzing** cellular network coverage and performance data

The application supports multiple network technologies (2G, 3G, 4G, 5G, NR-IoT) and provides rich visualizations with customizable colors, coverage polygons, and temporally organized metadata.

## ✨ Features

### 🗂️ Data Processing
- **CSV Import**: Support for CSV files with timestamps, CGIs, colors, and metadata
- **Smart Conversion**: Automatic color name conversion to KML hexadecimal format
- **Data Validation**: Integrity verification and handling of missing values

### 🌐 Database Integration
- **PostgreSQL Connection**: Access to databases with PostGIS extension
- **Complex Queries**: Optimized joins between multiple relational tables
- **Spatial Data**: Processing of geometries (points, polygons) with JTS Topology Suite

### 🗺️ KMZ Generation
- **Organized Structure**: Date-based folders with subfolders for points and polygons
- **Customizable Styles**: Dynamic colors based on CSV or technology
- **Directional Icons**: Rotated arrows according to antenna direction
- **Transparent Polygons**: Non-intrusive visualization with adjustable opacity
- **Complete Metadata**: Detailed descriptions with complete technical information

### 🎨 User Interface
- **Modern JavaFX**: Responsive and intuitive graphical interface
- **Asynchronous Operations**: Background processing without blocking UI
- **Visual Feedback**: Progress bar and operation status
- **File Selection**: Native dialogs for opening/saving files

## 🛠 Technologies

### Languages and Frameworks
- **Kotlin 1.9.22**: Main language with coroutines for concurrency
- **Java 21**: Execution platform
- **JavaFX**: Desktop graphical interface framework
- **Exposed ORM**: Database access framework

### Database
- **PostgreSQL 15**: Relational database management system
- **PostGIS 3.3**: Spatial extension for geographic data
- **JTS Topology Suite**: Library for spatial operations in Java

### Main Libraries
- `kotlinx-coroutines`: Asynchronous programming
- `kotlin-csv`: CSV file processing
- `Java API for KML`: KML/KMZ file generation
- `dotenv-kotlin`: Sensitive configuration management

## 🏗 Architecture

### Design Patterns
- **MVC (Model-View-Controller)**: Clear separation of responsibilities
- **Repository Pattern**: Data access abstraction
- **Extension Functions**: Extensibility of existing types
- **Data Classes**: Immutable models for data

### Main Components

```
KmzExporterApp (View)
         ↓
   MainController (Controller)
         ↓
Service Layer (CsvProcessor, DatabaseService, KmzGenerator)
         ↓
Domain Model (Cell, Location, Polygon, etc.)
         ↓
Data Access (Exposed ORM + PostGIS)
```

## 📥 Installation

### Prerequisites
- **Java 21** or higher
- **PostgreSQL 15** with **PostGIS 3.3** extension
- **Gradle 8.14** or higher

### Database Setup

1. **Install PostgreSQL and PostGIS**:
   ```sql
   CREATE DATABASE geocell;
   \c geocell;
   CREATE EXTENSION postgis;
   ```

2. **Run Database Scripts**:
   Check the `database/migrations` directory for required SQL scripts.

### Build and Execution

```bash
# Clone repository
git clone https://github.com/your-username/geocell-desktop-analyst.git
cd geocell-desktop-analyst

# Build project
./gradlew build

# Run application
./gradlew run
```

## ⚙ Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=geocell
DB_USER=your_username
DB_PASSWORD=your_password
```

### Input CSV Structure

The CSV file should contain the following columns:

```csv
timestamp,cgi,color,target,notes
2025/04/26 00:02:34,268-06-8840-8453,red,coverage,"Urban central cell"
2025/04/26 00:03:34,268-03-8521869,green,capacity,"High density area"
```

**Columns:**
- `timestamp`: Date and time in `YYYY/MM/DD HH:MM:SS` format
- `cgi`: Unique Cell Global Identity
- `color`: Color name or hexadecimal value (supports 150+ named colors)
- `target`: Cell classification or purpose
- `notes`: Additional observations or comments

## 🚀 Usage

### Graphical Interface

1. **Start Application**:
   ```bash
   ./gradlew run
   ```

2. **Select CSV File**:
   - Click "Select CSV File"
   - Choose the CSV file with cell data

3. **Export to KMZ**:
   - Click "Export to KMZ"
   - Choose output file location and name
   - Monitor progress in the progress bar

### Generated KMZ Structure

The resulting KMZ file will have the following organization:

```
📁 Cells Export
├── 📁 2025/04/26
│   ├── 📁 Points
│   │   ├── 📍 00:02:34 (cell with directional arrow)
│   │   ├── 📍 00:03:34
│   │   └── 📍 04:03:34
│   └── 📁 Polygons
│       ├── 🟢 00:02:34 - Polygon (coverage area)
│       ├── 🟢 00:03:34 - Polygon
│       └── 🟢 04:03:34 - Polygon
├── 📁 2025/04/27
│   ├── 📁 Points
│   └── 📁 Polygons
└── 🎨 Styles (colors and icons)
```

## 📁 Project Structure

```
src/main/kotlin/com/geocell/desktopanalyst/
├── controller/
│   └── MainController.kt          # Main orchestration
├── model/
│   ├── domain/                    # Business entities
│   │   ├── Cell.kt               # Network cell
│   │   ├── Location.kt           # Geographic location
│   │   ├── CellPolygon.kt        # Coverage polygon
│   │   ├── Band.kt               # Frequency band
│   │   ├── Country.kt            # Country
│   │   ├── District.kt           # District
│   │   ├── County.kt             # County/Municipality
│   │   ├── EnbGnb.kt             # Base station
│   │   ├── MCCMNC.kt             # Network operator
│   │   └── User.kt               # System user
│   └── table/                    # Database tables
│       ├── CellTable.kt
│       ├── LocationTable.kt
│       ├── CellPolygonTable.kt
│       ├── BandTable.kt
│       ├── CountryTable.kt
│       ├── DistrictTable.kt
│       ├── CountyTable.kt
│       ├── EnbGnbTable.kt
│       └── MccMncTable.kt
├── service/                      # Business logic
│   ├── CsvProcessor.kt           # CSV processing
│   ├── DatabaseService.kt        # Database access
│   └── KmzGenerator.kt           # KMZ generation
├── extensions/                   # Extension functions
│   └── ResultRowExtensions.kt    # ResultRow → Domain conversion
├── util/                         # Utilities
│   └── ColorConverter.kt         # Color conversion
└── KmzExporterApp.kt             # Main JavaFX application
```

## 📊 Data Models

### Geographic Hierarchy

```
Country (Country)
    ↓
District (District)
    ↓
County (County/Municipality)
    ↓
Location (Location)
    ↓
Cell (Cell) + EnbGnb (Base Station)
```

### Main Entities

#### Cell (Cell)
- **Identification**: CGI, LAC/TAC, ECI/NCI
- **Technology**: 2G, 3G, 4G, 5G, NR-IoT
- **Location**: Coordinates, antenna direction
- **Operator**: MCC/MNC, frequency bands
- **Metadata**: Timestamps, colors, targets, notes

#### Location (Location)
- **Coordinates**: Precise geographic point (WGS84)
- **Address**: Complete postal information
- **Context**: Insertion in administrative hierarchy

#### CellPolygon (Polygon)
- **Geometry**: Detailed coverage area
- **Optimization**: Simplified version for performance
- **Visualization**: Transparency and customizable colors

## 🛠 Development

### Environment Setup

1. **IntelliJ IDEA** (recommended):
   - Install Kotlin plugin
   - Configure JDK 21
   - Import as Gradle project

2. **Dependencies**:
   ```kotlin
   // build.gradle.kts
   dependencies {
       implementation(kotlin("stdlib"))
       implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
       implementation("org.jetbrains.exposed:exposed-core:0.44.0")
       implementation("org.postgresql:postgresql:42.7.7")
       implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")
   }
   ```

### Code Conventions

- **Naming**: camelCase for variables, PascalCase for classes
- **Documentation**: Complete docstrings in all public classes
- **Immutability**: Preference for data classes and val properties
- **Null Safety**: Consistent use of nullable types and safe calls

### Testing

```bash
# Run unit tests
./gradlew test

# Run tests with coverage
./gradlew jacocoTestReport
```

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Contribution Guidelines

- **Documentation**: Update README and docstrings as needed
- **Tests**: Add tests for new features
- **Code Style**: Follow existing project conventions
- **Commits**: Use descriptive messages in English

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions and support:

- **Issues**: [GitHub Issues](https://github.com/your-username/geocell-desktop-analyst/issues)
- **Email**: voicnanev@proton.me
- **Documentation**: Check in-code documentation and examples in `examples/` folder

## 🙏 Acknowledgments

- **Development Team**: For continuous contributions
- **Kotlin Community**: For excellent development ecosystem
- **Telecommunications Operators**: For feedback and real use cases

---

**GeoCell Desktop Analyst** - Professional tool for cellular network analysis and visualization 🏗️📶✨