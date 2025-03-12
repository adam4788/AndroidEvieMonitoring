# Zilla Hairball 2 Monitor - Android App

An Android application that connects via Bluetooth to display and monitor real-time metrics from a Zilla Hairball 2 interface in an electric vehicle. The app features a modern, sporty red theme designed for visibility and detailed monitoring.

## Features

- **Bluetooth Connectivity**: Connect to the Zilla Hairball 2 interface via Bluetooth adapter
- **Real-time Monitoring**: Display all critical vehicle metrics with visually appealing gauges 
- **Modern UI**: Dark theme with sporty red accents optimized for visibility in vehicles
- **Landscape Orientation**: Designed specifically for tablet mounting in a vehicle
- **Comprehensive Metrics**: Monitor battery voltage, current, motor performance, and more
- **Status Indicators**: Clear visualization of controller status and operating modes

## Metrics Displayed

- Battery voltage and current
- Motor voltage and current
- Motor speed (RPM)
- Power output (kW)
- Accelerator position
- Controller status
- Precharge circuit status
- Efficiency calculations
- Error codes and warnings

## Screenshots

*Screenshots will be available after the first build*

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture using modern Android development practices:

- **Jetpack Compose**: For a modern, declarative UI
- **Kotlin Coroutines & Flow**: For reactive programming and asynchronous operations
- **Bluetooth Communication**: Dedicated service for handling Bluetooth operations
- **Material Design**: Consistent design language with custom theming

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/zillahairballmonitor/
│   │   │   ├── bluetooth/       # Bluetooth communication layer
│   │   │   ├── data/            # Data models
│   │   │   ├── ui/              # UI components
│   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   ├── dashboard/   # Main dashboard screen
│   │   │   │   └── theme/       # Theme definitions
│   │   │   ├── viewmodel/       # ViewModels
│   │   │   └── MainActivity.kt  # Entry point
│   │   ├── res/                 # Android resources
│   │   └── AndroidManifest.xml  # App manifest
│   └── test/                    # Unit tests
└── build.gradle                 # App-level build configuration
```

## Requirements

- Android 9 (API level 28) or higher
- Device with Bluetooth capability
- Zilla Hairball 2 interface with Bluetooth adapter

## Building and Running

1. Clone the repository
2. Open the project in Android Studio
3. Build and run on a compatible Android device or emulator

## Development Notes

- The app includes a simulation mode for testing without a physical Zilla controller
- The Bluetooth implementation uses the Serial Port Profile (SPP) for communication
- Real-world performance may vary based on the specific Bluetooth adapter used

## License

This project is provided as open source. Feel free to modify and distribute according to your needs.

## Acknowledgements

- Created for use with the Zilla motor controller system by Manzanita Micro
- Developed to assist EV enthusiasts with real-time vehicle monitoring
