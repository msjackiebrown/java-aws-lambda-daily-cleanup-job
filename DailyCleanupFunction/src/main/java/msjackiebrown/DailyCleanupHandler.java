package msjackiebrown;

import msjackiebrown.helpers.S3ClientHelper;
import msjackiebrown.helpers.SnsClientHelper;
import msjackiebrown.helpers.CloudWatchHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DailyCleanupHandler {

    private static final Logger logger = LogManager.getLogger(DailyCleanupHandler.class);

    private final S3ClientHelper s3Helper;
    private final SnsClientHelper snsHelper;
    private final CloudWatchHelper cloudWatchHelper;
    private boolean dryRun = false;  // Add this line

    // Add getter/setter
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public DailyCleanupHandler() {
        S3Client s3Client = S3Client.create();
        SnsClient snsClient = SnsClient.create();
        CloudWatchClient cloudWatchClient = CloudWatchClient.create();
        this.s3Helper = new S3ClientHelper(s3Client);
        this.snsHelper = new SnsClientHelper(snsClient);
        this.cloudWatchHelper = new CloudWatchHelper(cloudWatchClient);
    }

    public String handleRequest() {
        try {
            logger.info("Starting daily cleanup process...");

            // Read environment variables
            String bucketName = System.getenv("BUCKET_NAME");
            String daysEnv = System.getenv("DAYS");
            String dryRunEnv = System.getenv("DRY_RUN");
            
            // Set dry run mode from environment variable
            this.dryRun = "true".equalsIgnoreCase(dryRunEnv);
            logger.info("Dry run mode: {}", this.dryRun);

            // Validate environment variables
            if (bucketName == null || bucketName.isEmpty()) {
                return "Environment variable BUCKET_NAME is not set.";
            }
            if (!bucketName.matches("^[a-zA-Z0-9.-]{3,63}$")) {
                return "Invalid BUCKET_NAME. Ensure it follows S3 bucket naming conventions.";
            }
            if (daysEnv == null || daysEnv.isEmpty()) {
                return "Environment variable DAYS is not set.";
            }

            // Parse the days value
            int days;
            try {
                days = Integer.parseInt(daysEnv);
            } catch (NumberFormatException e) {
                return "Environment variable DAYS must be a valid integer.";
            }
            if (days <= 0) {
                return "Environment variable DAYS must be a positive integer.";
            }

            // Calculate the cutoff time
            Instant cutoffTime = Instant.now().minus(days, ChronoUnit.DAYS);

            // List and delete objects
            List<S3Object> objects = s3Helper.listObjects(bucketName);
            StringBuilder response = new StringBuilder();
            response.append(dryRun ? "[DRY RUN] " : "")
                   .append("Objects to be deleted from bucket ")
                   .append(bucketName).append(":\n");
            
            int filesDeleted = 0;
            long totalBytesDeleted = 0;

            for (S3Object s3Object : objects) {
                if (s3Object.lastModified().isBefore(cutoffTime)) {
                    if (!dryRun) {
                        s3Helper.deleteObject(bucketName, s3Object.key());
                    }
                    response.append(dryRun ? "[WOULD DELETE] " : "[DELETED] ")
                           .append(String.format("%s (last modified: %s)", 
                               s3Object.key(), 
                               s3Object.lastModified().toString()))
                           .append("\n");
                    filesDeleted++;
                    totalBytesDeleted += s3Object.size();
                }
            }

            // Publish metrics only if not in dry-run mode
            if (filesDeleted > 0 && !dryRun) {
                cloudWatchHelper.publishMetrics(bucketName, filesDeleted, totalBytesDeleted);
            }

            response.append(String.format(
                "%s%d files would be deleted (total size: %d bytes)\n",
                dryRun ? "[DRY RUN] " : "",
                filesDeleted,
                totalBytesDeleted
            ));

            logger.info("Response: {}", response.toString());
            logger.info("Cleanup completed successfully.");
            return response.toString();

        } catch (Exception e) {
            logger.error("Error during cleanup process: {}", e.getMessage(), e);

            // Send an SNS notification
            String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
            snsHelper.sendNotification(snsTopicArn, "Daily Cleanup Job Failed", "Error during cleanup process: " + e.getMessage());

            return "Error processing request: " + e.getMessage();
        }
    }
    
}
