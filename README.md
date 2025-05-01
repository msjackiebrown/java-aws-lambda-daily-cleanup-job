# Daily S3 Cleanup Lambda Function

An AWS Lambda function that automatically cleans up old files from an S3 bucket based on configurable age threshold.

## Features

- Deletes files older than specified number of days from S3 bucket
- Supports dry-run mode to preview deletions without making changes
- Filters files by extension (e.g., .log, .tmp, .bak files)
- Sends SNS notifications on failures
- Publishes CloudWatch metrics for monitoring
- Scheduled to run daily at midnight UTC

## Project Architecture

### Component Overview
```
java-daily-cleanup-job/
├── DailyCleanupFunction/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── msjackiebrown/
│   │               ├── DailyCleanupHandler.java    # Main Lambda handler
│   │               └── helpers/
│   │                   ├── S3ClientHelper.java      # S3 operations
│   │                   ├── SnsClientHelper.java     # SNS notifications
│   │                   └── CloudWatchHelper.java    # Metrics publishing
│   ├── pom.xml                                     # Maven configuration
│   └── template.yaml                               # SAM template
└── README.md
```

### Architectural Flow
1. **CloudWatch Event** triggers the Lambda function daily
2. **DailyCleanupHandler**:
   - Validates environment variables
   - Calculates cutoff time based on DAYS parameter
   - Lists objects from S3 bucket
3. **S3ClientHelper**:
   - Lists and deletes objects from S3
4. **CloudWatchHelper**:
   - Publishes metrics about deleted files
5. **SnsClientHelper**:
   - Sends notifications on failures

### AWS Services Integration
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

## Prerequisites

- Java 21
- Maven
- AWS CLI
- AWS SAM CLI
- An AWS account with appropriate permissions

## Environment Variables

- `BUCKET_NAME`: Name of the S3 bucket to clean up
- `DAYS`: Number of days to retain files (files older than this will be deleted)
- `SNS_TOPIC_ARN`: ARN of SNS topic for notifications
- `DRY_RUN`: Set to "true" to enable dry-run mode (default: "false")
- `FILE_TYPES`: Comma-separated list of file extensions to clean up (e.g., ".log,.tmp,.bak"). If not set, all files are processed
- `PREFIXES`: Comma-separated list of S3 prefixes/folders to clean up (e.g., "logs/,temp/"). If not set, all folders are processed

## Configuration Examples

### Clean up only log files older than 30 days:
```sh
BUCKET_NAME=my-bucket
DAYS=30
FILE_TYPES=.log
```

### Clean up temporary and backup files older than 7 days:
```sh
BUCKET_NAME=my-bucket
DAYS=7
FILE_TYPES=.tmp,.bak
```

### Clean up log files in specific folders:
```sh
BUCKET_NAME=my-bucket
DAYS=30
PREFIXES=logs/system/,logs/application/
FILE_TYPES=.log
```

### Clean up temporary files in staging area:
```sh
BUCKET_NAME=my-bucket
DAYS=1
PREFIXES=temp/staging/
FILE_TYPES=.tmp
```

### Clean up all files in archive folders older than 90 days:
```sh
BUCKET_NAME=my-bucket
DAYS=90
PREFIXES=archive/2023/,archive/2022/
```

### Preview deletion of log files (dry run):
```sh
BUCKET_NAME=my-bucket
DAYS=30
FILE_TYPES=.log
DRY_RUN=true
```

## Building

Build the project using Maven:

```sh
cd DailyCleanupFunction
mvn clean package
```

## Deployment

Deploy using AWS SAM:

```sh
sam build
sam deploy --guided
```

## Testing

Run tests using Maven:

```sh
mvn test
```

## IAM Permissions

The function requires these AWS permissions:
- S3: ListBucket, GetObject, DeleteObject
- SNS: Publish
- CloudWatch: PutMetricData
- CloudWatch Logs: CreateLogGroup, CreateLogStream, PutLogEvents

## Monitoring

The function publishes these CloudWatch metrics under the "S3DailyCleanup" namespace:
- FilesDeleted: Number of files deleted
- BytesDeleted: Total size of deleted files