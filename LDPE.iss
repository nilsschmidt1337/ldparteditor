#define MyAppName "LDPartEditor"
#define MyAppVersion "0.8.44b"
#define MyAppPublisher "Nils Schmidt"
#define MyAppExeName "LDPartEditor.exe"
#define MyExtension "*.dat"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId=1E5BAA6E-AFEF-48F6-8BDE-F98C3F1CBA1E
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={pf}\LDPartEditor
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=I:\LDPE\LICENSE
OutputDir=I:\LDPE
OutputBaseFilename=LDPE_{#MyAppVersion}_Setup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "japanese"; MessagesFile: "compiler:Languages\Japanese.isl"
Name: "danish"; MessagesFile: "compiler:Languages\Danish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "associate"; Description: "{cm:AssocFileExtension,{#MyAppName},{#MyExtension}}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "I:\LDPE\LDPartEditor.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\glfw.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\jemalloc.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\lwjgl.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\OpenAL.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\LICENSE"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\categories.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "I:\LDPE\primitive_rules.txt"; DestDir: "{app}"; Flags: ignoreversion

[Dirs]
Name: "{app}\plugin"

[UninstallDelete]
Type: filesandordirs; Name: "{app}"

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, "&", "&&")}}"; Flags: nowait postinstall skipifsilent

[Registry]
Root: HKCR; Subkey: ".dat"; ValueType: string; ValueName: ; ValueData: "datfile"; Flags: uninsdeletekey; Tasks: associate
Root: HKCR; Subkey: "datfile\shell\open\command"; ValueType: string; ValueName: ; ValueData: "{app}\{#MyAppExeName} %1"; Flags: uninsdeletekey; Tasks: associate
