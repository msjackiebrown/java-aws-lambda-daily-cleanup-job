# S3 Cleanup Lambda Function

> Automatically clean up old files from an S3 bucket based on configurable rules and schedules.

## 📋 Quick Start

```bash
# 1. Build the project
cd DailyCleanupFunction
mvn clean package

# 2. Deploy with default settings (midnight UTC)
sam build
sam deploy --guided
```

## ✨ Features

- 🗑️ Automatic deletion of old files from S3 buckets
- ⏰ Configurable cleanup schedules
- 🔍 Dry-run mode for previewing changes
- 🔎 File filtering by extension (e.g., .log, .tmp, .bak)
- 📩 SNS notifications for failures
- 📊 CloudWatch metrics for monitoring

## 🏗️ Architecture

### Components
```
java-daily-cleanup-job/
├── DailyCleanupFunction/
│   ├── src/main/java/msjackiebrown/
│   │   ├── DailyCleanupHandler.java    # Main Lambda handler
│   │   └── helpers/
│   │       ├── S3ClientHelper.java      # S3 operations
│   │       ├── SnsClientHelper.java     # SNS notifications
│   │       └── CloudWatchHelper.java    # Metrics publishing
│   ├── pom.xml                         # Maven configuration
│   └── template.yaml                   # SAM template
└── README.md
```

### Flow Diagram
```
┌─────────────────┐    ┌─────────────────┐
│ CloudWatch Event│───>│  Lambda Function │
└─────────────────┘    └────────┬────────┘
                                │
                    ┌───────────┴───────────┐
                    ▼           ▼           ▼
             ┌──────────┐ ┌──────────┐ ┌────────┐
             │    S3    │ │CloudWatch│ │  SNS   │
             └──────────┘ └──────────┘ └────────┘
```

## 🛠️ Prerequisites

- ☕ Java 21
- 📦 Maven
- 🔧 AWS CLI
- 🔨 AWS SAM CLI
- 💳 AWS Account with appropriate permissions

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `BUCKET_NAME` | S3 bucket to clean | *Required* |
| `DAYS` | Retention period in days | *Required* |
| `SNS_TOPIC_ARN` | SNS topic for notifications | *Required* |
| `DRY_RUN` | Preview mode | `false` |
| `FILE_TYPES` | Extensions to clean (`.log,.tmp`) | All files |
| `PREFIXES` | Folders to clean (`logs/,temp/`) | All folders |
| `CLEANUP_SCHEDULE` | Cron schedule | `cron(0 0 * * ? *)` |

### Schedule Examples

```bash
# Daily at midnight UTC
CLEANUP_SCHEDULE="cron(0 0 * * ? *)"

# Every 6 hours
CLEANUP_SCHEDULE="cron(0 */6 * * ? *)"

# Weekdays at 8am UTC
CLEANUP_SCHEDULE="cron(0 8 ? * MON-FRI *)"
```

### Common Use Cases

1. **Log File Cleanup (30 days)**
   ```bash
   BUCKET_NAME=my-bucket
   DAYS=30
   FILE_TYPES=.log
   ```

2. **Temp File Cleanup (7 days)**
   ```bash
   BUCKET_NAME=my-bucket
   DAYS=7
   FILE_TYPES=.tmp,.bak
   ```

3. **Archive Cleanup (90 days)**
   ```bash
   BUCKET_NAME=my-bucket
   DAYS=90
   PREFIXES=archive/2023/,archive/2022/
   ```

## 📦 Deployment

### Using SAM CLI

```bash
# Windows CMD
sam deploy --parameter-overrides CleanupSchedule="cron(0 12 * * ? *)"

# PowerShell
sam deploy --parameter-overrides CleanupSchedule='cron(0 12 * * ? *)'
```

### Using samconfig.toml
```toml
version = 0.1
[default.deploy.parameters]
stack_name = "java-daily-cleanup-job"
resolve_s3 = true
region = "us-east-1"
confirm_changeset = true
capabilities = "CAPABILITY_IAM"
parameter_overrides = [
    "CleanupSchedule=\"cron(0 12 * * ? *)\""
]
```

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Local testing
sam local invoke -e events/schedule-test.json
```

## 🔐 IAM Permissions

Required AWS permissions:
- **S3**: `ListBucket`, `GetObject`, `DeleteObject`
- **SNS**: `Publish`
- **CloudWatch**: `PutMetricData`
- **CloudWatch Logs**: `CreateLogGroup`, `CreateLogStream`, `PutLogEvents`
- **EventBridge**: `PutRule`

## 📊 Monitoring

CloudWatch metrics (`S3DailyCleanup` namespace):
- `FilesDeleted`: Number of files deleted
- `BytesDeleted`: Total size of deleted files

## 🌐 Configuration Web Interface

Visit our web interface for easy configuration:
```
https://msjackiebrown.github.io/java-daily-cleanup-job/
```

Features:
- 📝 Plain English schedule configuration
- 🎯 Bucket and retention settings
- ⚡ Instant cron expression generation
- 📋 Deployment command previews