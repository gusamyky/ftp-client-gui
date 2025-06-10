# FTP Client GUI

A modern, user-friendly FTP client application built with JavaFX that provides a graphical interface for managing file transfers and server interactions.

## Project Overview

This FTP client application offers a comprehensive set of features for managing file transfers and server interactions through an intuitive graphical user interface. The application is built using JavaFX and follows modern software architecture patterns.

### Key Features

- User authentication (login/registration)
- File management (upload/download)
- Command console for direct server interaction
- Operation history tracking
- File filtering and search
- Progress tracking for file transfers
- Error handling and user notifications
- Modern, responsive UI design

## Installation and Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- JavaFX 21.0.2

### Installation Steps

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd ftp-client-gui
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

## Usage

### Authentication

1. Launch the application
2. Use the "Logowanie" (Login) tab to sign in with existing credentials
3. Use the "Rejestracja" (Registration) tab to create a new account

### File Management

1. After logging in, navigate to the "Pliki" (Files) tab
2. Use the file list to view available files on the server
3. Use the filter field to search for specific files
4. Click the upload button to send files to the server
5. Click on files in the list to download them

### Command Console

1. Access the "Konsola" (Console) tab
2. Enter commands in the input field
3. View command responses in the console area

### History

1. Access the "Historia" (History) tab
2. View operation history
3. Use the export/import buttons to save or load history

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── ftp/
│   │       └── gusamyky/
│   │           └── client/
│   │               ├── controller/    # UI controllers
│   │               ├── model/         # Data models
│   │               ├── service/       # Business logic
│   │               └── util/          # Utility classes
│   └── resources/
│       ├── client/    # FXML and CSS files
│       └── config.properties
└── test/             # Test files
```

### Key Components

- **Controllers**: Handle UI interactions and user input
- **Models**: Define data structures and application state
- **Services**: Implement business logic and network communication
- **Utils**: Provide helper functions and common utilities

## Development Guidelines

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Include appropriate comments and documentation
- Maintain consistent formatting

### Building and Testing

1. Build the project:
   ```bash
   mvn clean install
   ```

2. Run tests:
   ```bash
   mvn test
   ```

### Error Handling

The application implements comprehensive error handling:

- Network errors are caught and displayed to users
- File operation errors are handled gracefully
- User input validation is performed
- Error messages are clear and actionable

## Configuration

The application can be configured through `config.properties`:

```properties
# Server Configuration
server.host=0.0.0.0
server.port=2121
server.files.dir=server_files

# Client Configuration
client.files.dir=client_files
connection.timeout=30000

# Cloud Connection Settings
cloud.retry.attempts=3
cloud.retry.delay=5000
cloud.keepalive.interval=30000
```

## Troubleshooting

### Common Issues

1. **Connection Errors**
   - Verify server is running
   - Check network connectivity
   - Verify server host and port in config

2. **File Transfer Issues**
   - Check file permissions
   - Verify sufficient disk space
   - Ensure file isn't in use by another process

3. **Authentication Problems**
   - Verify username and password
   - Check server authentication settings
   - Ensure account is active

## Credits

- JavaFX for the GUI framework
- Maven for build management
- [List other dependencies and contributors] 