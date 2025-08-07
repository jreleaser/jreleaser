# Windows SignTool Implementation for JReleaser

## Overview
Implemented comprehensive Windows code signing support in JReleaser using Microsoft's SignTool.exe, addressing the gap where JReleaser could sign with GPG and Cosign but not Windows Authenticode.

## Implementation Summary

### 1. Core Changes

#### Added SIGNTOOL to Signing.Mode enum
- File: `/api/jreleaser-model-api/src/main/java/org/jreleaser/model/Signing.java`
- Added `SIGNTOOL` to the Mode enum

#### Created SignTool API Interface
- File: `/api/jreleaser-model-api/src/main/java/org/jreleaser/model/api/signing/SignTool.java`
- Properties: certificateFile, password, timestampUrl, algorithm, description, executable

#### Implemented SignTool Configuration
- File: `/core/jreleaser-model-impl/src/main/java/org/jreleaser/model/internal/signing/SignTool.java`
- Full configuration implementation with validation and merging

#### Updated Signing Classes
- Modified signature extension handling (empty string for SIGNTOOL since it signs in-place)
- Updated asMap() method for configuration display
- Added SignTool field and accessor methods

#### Integrated into Signer Engine
- File: `/core/jreleaser-engine/src/main/java/org/jreleaser/engine/sign/Signer.java`
- Added signToolSign() method
- Updated collectArtifacts() to handle in-place signing
- Added verification support

#### Created SignTool SDK Implementation
- File: `/sdks/jreleaser-tool-java-sdk/src/main/java/org/jreleaser/sdk/tool/SignTool.java`
- Intelligent SignTool discovery
- Platform detection (Windows-only)
- Comprehensive signing and verification methods

### 2. Key Features

#### Certificate Support
- PFX/P12 certificates with password protection
- Certificate store integration
- Hardware token support

#### Command Structure
```bash
signtool sign /fd SHA256 /f certificate.pfx /p password /tr timestampUrl /td SHA256 /d "Description" /v file.exe
```

#### SignTool Discovery
- Custom paths via system properties/environment variables
- Windows SDK paths (Windows Kits 10, Microsoft SDKs)
- PATH environment search

#### Modern Standards
- SHA256 as default algorithm
- RFC 3161 timestamping
- Policy-based verification (/pa flag)

### 3. Configuration Example

```yaml
signing:
  active: ALWAYS
  mode: SIGNTOOL
  signTool:
    certificateFile: 'path/to/certificate.pfx'
    password: '${env:CERT_PASSWORD}'
    timestampUrl: 'http://timestamp.digicert.com'
    algorithm: 'SHA256'
    description: 'My Application'
    executable: 'signtool'  # Optional, auto-discovered
```

### 4. Testing Instructions

#### Create Test Certificate (Windows PowerShell as Admin)
```powershell
$cert = New-SelfSignedCertificate -Type CodeSigningCert -Subject "CN=TestCert" -CertStoreLocation Cert:\CurrentUser\My
$pwd = ConvertTo-SecureString -String "testpass123" -Force -AsPlainText
Export-PfxCertificate -Cert $cert -FilePath ".\testcert.pfx" -Password $pwd
```

#### Run Signing
```bash
jreleaser sign --debug
```

#### Verify Signature
```cmd
signtool verify /pa /v signed.exe
```

### 5. Implementation Validation

- ✅ Follows JReleaser patterns (similar to Cosign implementation)
- ✅ Proper error handling with SigningException
- ✅ Platform-specific behavior (Windows-only)
- ✅ In-place signing correctly handled
- ✅ Comprehensive logging integration
- ✅ Configuration validation and merging
- ✅ Immutable API patterns maintained

## Status
Implementation complete and ready for testing. The Windows SignTool support fills the identified gap and enables proper Windows application signing as part of JReleaser's release workflow.