package com.whatsmode.analytics;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.*;
import com.google.api.services.analyticsreporting.v4.model.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class test {

    public static final String APPLICATION_NAME = "Google Api for developer";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final String GOOGLE_ANALYTICS_REPORTING_API_KEY_FILE_LOCATION =
            "classpath:XXXXX.json";//Write your key.json name
    public static final String VIEW_ID = "ga:XXXXXXXXX";//Write your view id
    public static final int PAGE_SIZE = 10000;

    //Create the Product Dimensions object.
    public static final Dimension[] PRODUCT_DIMENSIONS = new Dimension[]{
            new Dimension().setName("ga:date"),
            new Dimension().setName("ga:source"),
            new Dimension().setName("ga:productName"),
            new Dimension().setName("ga:productSku"),
            new Dimension().setName("ga:productBrand")
    };

    // Create the Product Metrics object.
    public static final Metric[] PRODUCT_METRICS = new Metric[]{
            new Metric().setExpression("ga:productAddsToCart"),
            new Metric().setExpression("ga:productCheckouts"),
            new Metric().setExpression("ga:productDetailViews"),
            new Metric().setExpression("ga:itemRevenue"),
            new Metric().setExpression("ga:productRevenuePerPurchase"),
            new Metric().setExpression("ga:itemQuantity"),
            new Metric().setExpression("ga:quantityAddedToCart"),
            new Metric().setExpression("ga:quantityCheckedOut"),
            new Metric().setExpression("ga:uniquePurchases")
    };

    public static void main(String[] args) {
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();

            GetReportsResponse response = getReport(service);
            printResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Initializes an authorized Analytics Reporting service object.
    private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport;

        //If you are in China, you need proxy to access google
        httpTransport = new NetHttpTransport.Builder()
                .trustCertificates(GoogleUtils.getCertificateTrustStore())
                .setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("XXX.X.X.X", XXXX)))
                .build();
        //Replace your hostname and port

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
                new PathMatchingResourcePatternResolver();
        Resource resource = pathMatchingResourcePatternResolver.getResource
                (GOOGLE_ANALYTICS_REPORTING_API_KEY_FILE_LOCATION);

        GoogleCredential credential = GoogleCredential
                .fromStream(resource.getInputStream(), httpTransport, JSON_FACTORY)
                .createScoped(AnalyticsReportingScopes.all());

        // Construct the Analytics Reporting service object.
        return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    private static GetReportsResponse getReport(AnalyticsReporting service) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("XXXX-XX-XX");//Wrtte your date
        dateRange.setEndDate("XXXX-XX-XX");

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(VIEW_ID)
                .setDateRanges(Arrays.asList(dateRange))
                .setDimensions(Arrays.asList(PRODUCT_DIMENSIONS))
                .setMetrics(Arrays.asList(PRODUCT_METRICS));

        ArrayList<ReportRequest> requests = new ArrayList<>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();
        // Return the response.
        return response;
    }

    private static void printResponse(GetReportsResponse response) {

        for (Report report: response.getReports()) {
            ColumnHeader header = report.getColumnHeader();
            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            if (rows == null) {
                System.out.println("No data found for " + VIEW_ID);
                return;
            }

            for (ReportRow row: rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();
                //Get Dimension
                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                }

                //Get Metrics
                for (int j = 0; j < metrics.size(); j++) {
                    System.out.print("Date Range (" + j + "): ");
                    DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
                    }
                }

            }
        }
    }
}
