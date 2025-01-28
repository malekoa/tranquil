# tranquil

**tranquil** is a simple CLI tool that generates poems of various types (e.g., haiku, sonnet, limerick) using OpenAI's API. The poems can be customized, or a random poem is given.

## Installation

As of now, you must build the standalone JAR file yourself. Using leinengen:

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/tranquil.git
   cd tranquil
   ```
2. Build the JAR file:
   ```bash
   lein uberjar
   ```
3. The standalone JAR will be located in `target/tranquil-0.1.0-standalone.jar`.

## Usage

### Running the Application

#### Using Leiningen
For development:
```bash
lein trampoline run -- COMMAND [options]
```

#### Using the JAR
Run the standalone JAR:
```bash
java -jar tranquil-0.1.0-standalone.jar [options]
```

### CLI Options

| Option           | Description                                                                                     |
|-------------------|-------------------------------------------------------------------------------------------------|
| `-h, --help`      | Show help and usage information.                                                               |
| `-v, --version`   | Display the current version of tranquil.                                                      |
| `-t, --type TYPE` | Specify the type of poem to generate. Options: `haiku`, `sonnet`, `limerick`. **(Required)**   |
| `-p, --prompt PROMPT` | What the poem should be about. If not specified, the poem will be on a random topic. **(Optional)** |

### Configuration

When you first run tranquil, you'll be prompted to provide an OpenAI API key. This key is stored in the following location:

```plaintext
~/.config/tranquil/config.json
```

If your API key is invalid or revoked, you’ll be prompted to enter a new one.

### Examples

#### Generate a Haiku on a Random Topic
```bash
java -jar tranquil-0.1.0-standalone.jar -t haiku
```

#### Generate a Limerick About "cats"
```bash
java -jar tranquil-0.1.0-standalone.jar -t limerick -p "cats"
```

#### Generate a Sonnet About "love"
```bash
java -jar tranquil-0.1.0-standalone.jar -t sonnet -p "love"
```

#### Show Help
```bash
java -jar tranquil-0.1.0-standalone.jar --help
```

#### Display Version
```bash
java -jar tranquil-0.1.0-standalone.jar --version
```

## Roadmap

Here are planned improvements for tranquil:

1. **Add Unit and Integration Tests**:
   - Ensure all core functionality, including CLI commands and OpenAI API integration, is tested.

2. **Improve Error Handling**:
   - Provide detailed messages for different failure scenarios (e.g., network errors, invalid API responses).

3. **Extend Poem Types**:
   - Add support for more types of poems, such as ballads or odes.

4. **Add Prebuilt Releases**:
   - Provide prebuilt binaries for easy installation.

5. **Configuration Management**:
   - Allow users to manage and update configuration via CLI commands.

6. **Add Logging**:
   - Introduce a logging system to aid debugging and monitor usage.

7. **Documentation**:
   - Expand this README with detailed usage examples and development guides.