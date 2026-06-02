# IntelliJ IDEA MCP Server

## Overview

The JetBrains IDE (IntelliJ IDEA 2025.1.7.1 Community) runs a built-in web server on `localhost:63342`
with the deprecated MCP Server Plugin (v1.0.30) exposing IDE capabilities via a custom HTTP API at
`/api/mcp`.  Since 2025.2, MCP is built into IntelliJ natively with standard SSE / Stdio / HTTP
Stream transports.

## Transport Protocol (custom — not standard MCP JSON-RPC)

| Method | When               | Parameters                           |
|--------|--------------------|--------------------------------------|
| `GET`  | No-argument tools  | none                                 |
| `POST` | Tools with args    | JSON body, `Content-Type: application/json` |

### Response format

- **Success** (most tools): `{"status": "<result string>"}`
- **Error**:           `{"error": "<error message>"}`
- **`list_tools`**:    raw JSON array (not wrapped in `status`/`error`)

---

## PowerShell usage

`Invoke-WebRequest` silently mangles POST bodies on this API.  Use .NET `HttpClient` directly:

```powershell
Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient

# POST with JSON body
$body = '{"pathInProject":"/"}'
$content = New-Object System.Net.Http.StringContent($body, [System.Text.Encoding]::UTF8, "application/json")
$response = $client.PostAsync("http://localhost:63342/api/mcp/list_files_in_folder", $content).Result
$response.Content.ReadAsStringAsync().Result

# GET (no args)
$response = $client.GetAsync("http://localhost:63342/api/mcp/get_open_in_editor_file_path").Result
$response.Content.ReadAsStringAsync().Result

$client.Dispose()
```

---

## Tool reference — 36 tools

### Listing & discovery

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `list_tools` **GET** | none | Raw JSON array of `{name, description, inputSchema}` |

### File operations (current editor)

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `get_open_in_editor_file_path` **GET** | — | Absolute path |
| `get_open_in_editor_file_text` **GET** | — | Full text content |
| `get_selected_in_editor_text` **GET** | — | Selected text |
| `replace_selected_text` **POST** | `{text}` | `"ok"` / error |
| `replace_current_file_text` **POST** | `{text}` | `"ok"` / error |
| `get_current_file_errors` **GET** | — | `[{severity, description, lineContent}]` |
| `reformat_current_file` **GET/POST** | — / `{}` | `"ok"` |

### File operations (by path)

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `get_file_text_by_path` **POST** | `{pathInProject}` | File content |
| `replace_file_text_by_path` **POST** | `{pathInProject, text}` | `"ok"` / error |
| `replace_specific_text` **POST** | `{pathInProject, oldText, newText}` | `"ok"` / error |
| `create_new_file_with_text` **POST** | `{pathInProject, text}` | `"ok"` / error |
| `reformat_file` **POST** | `{pathInProject}` | `"ok"` |

### Multi-file / bulk

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `get_all_open_file_paths` **GET** | — | Newline-separated list |
| `get_all_open_file_texts` **GET** | — | `[{path, text}]` |

### Project navigation & search

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `list_files_in_folder` **POST** | `{pathInProject}` (use `"/"` for root) | `[{name, type, path}]` |
| `list_directory_tree_in_folder` **POST** | `{pathInProject, maxDepth?}` | Nested `[{name, type, path, children}]` |
| `find_files_by_name_substring` **POST** | `{nameSubstring}` | `[{path, name}]` |
| `search_in_files_content` **POST** | `{searchText}` | `[{path, name}]` |
| `get_project_modules` **GET** | — | JSON list of module names |
| `get_project_dependencies` **GET** | — | JSON list of dependency names |

### VCS & debug

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `get_project_vcs_status` **GET** | — | `[{path, type}]` (MODIFICATION/ADDITION/DELETION/MOVED) |
| `toggle_debugger_breakpoint` **POST** | `{filePathInProject, line}` | `"ok"` |
| `get_debugger_breakpoints` **GET** | — | `[{path, line}]` |
| `find_commit_by_message` **POST** | `{query}` | JSON array of commit hashes |

### Run & execute

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `get_run_configurations` **GET** | — | JSON list of configuration names |
| `run_configuration` **POST** | (configuration name from above) | Stdout/stderr prefixed `"ok\n..."` |
| `execute_action_by_id` **POST** | `{actionId}` | `"ok"` |
| `list_available_actions` **GET** | — | `[{id, text}]` |

### Terminal

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `get_terminal_text` **GET** | — | Terminal buffer |
| `execute_terminal_command` **POST** | `{command}` | Output (capped at 2000 lines, 120s timeout) |

### Utility

| Tool / method | Params | Returns |
|---------------|--------|---------|
| `wait` **POST** | `{milliseconds?}` (default 5000) | `"ok"` |
| `get_progress_indicators` **GET** | — | `[{text, fraction, indeterminate}]` |
| `get_project_problems` **GET** | — | `[{group, description, problemText}]` |
| `open_file_in_editor` **POST** | `{filePath, text}` | `"file is opened"` |

---

## Parameter conventions

| Concept       | Param name              | Notes                                      |
|---------------|-------------------------|--------------------------------------------|
| File path     | `pathInProject`         | Relative to project root, e.g. `"src/Foo.java"` |
| Root dir      | `"/"`                   | For `list_files_in_folder` etc.            |
| Text content  | `text`                  | Replacement / new content                  |
| Search        | `nameSubstring`, `searchText`, `query` | Case-insensitive substring |
| Numeric       | `line`, `maxDepth`, `milliseconds` | 1-based lines           |
| Action ID     | `actionId`              | From `list_available_actions`              |

---

## Architecture notes

- This is **not** standard MCP JSON-RPC.  It is a custom HTTP API that the JetBrains
  `@jetbrains/mcp-proxy` package translates to/from standard MCP over stdio.
- To connect via standard MCP (e.g. for opencode), run:
  `npx -y @jetbrains/mcp-proxy`
  This starts a stdio MCP server that proxies to the IDE.
- Since IntelliJ 2025.2, MCP is built-in with native SSE / Stdio / HTTP Stream transport.
  No plugin required.
