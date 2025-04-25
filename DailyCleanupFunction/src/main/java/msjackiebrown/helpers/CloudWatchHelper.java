package msjackiebrown.helpers;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import java.time.Instant;

public class CloudWatchHelper {
    private final CloudWatchClient cloudWatchClient;

    public CloudWatchHelper(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public void publishMetrics(String bucketName, int filesDeleted, long totalBytesDeleted) {
        Dimension dimension = Dimension.builder()
                .name("BucketName")
                .value(bucketName)
                .build();

        MetricDatum filesDeletedMetric = MetricDatum.builder()
                .metricName("FilesDeleted")
                .value((double) filesDeleted)
                .unit(StandardUnit.COUNT)
                .timestamp(Instant.now())
                .dimensions(dimension)
                .build();

        MetricDatum bytesDeletedMetric = MetricDatum.builder()
                .metricName("BytesDeleted")
                .value((double) totalBytesDeleted)
                .unit(StandardUnit.BYTES)
                .timestamp(Instant.now())
                .dimensions(dimension)
                .build();

        cloudWatchClient.putMetricData(PutMetricDataRequest.builder()
                .namespace("S3DailyCleanup")
                .metricData(filesDeletedMetric, bytesDeletedMetric)
                .build());
    }
}