package org.apache.commons.math.stat.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.exception.ConvergenceException;
import org.junit.Test;
import static org.junit.Assert.*;

public class KMeansPlusPlusClustererTest {

    private static class TestPoint implements Clusterable<TestPoint> {
        private final double[] point;

        public TestPoint(double... point) {
            this.point = point;
        }

        public double distanceFrom(TestPoint other) {
            double sum = 0;
            for (int i = 0; i < point.length; i++) {
                double diff = point[i] - other.point[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        }

        public TestPoint centroidOf(Collection<TestPoint> points) {
            double[] centroid = new double[point.length];
            for (TestPoint p : points) {
                for (int i = 0; i < point.length; i++) {
                    centroid[i] += p.point[i];
                }
            }
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] /= points.size();
            }
            return new TestPoint(centroid);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TestPoint)) {
                return false;
            }
            TestPoint other = (TestPoint) obj;
            return Arrays.equals(point, other.point);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(point);
        }

        @Override
        public String toString() {
            return Arrays.toString(point);
        }
    }

    // Tests normal clustering into 2 distinct clusters
    @Test
    public void testCluster_twoDistinctClusters_correctlySeparates() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(12345L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.0, 0.0),
            new TestPoint(0.1, 0.1),
            new TestPoint(0.0, 0.1),
            new TestPoint(10.0, 10.0),
            new TestPoint(10.1, 10.0),
            new TestPoint(10.0, 10.1)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertEquals(2, clusters.size());

        int size0 = clusters.get(0).getPoints().size();
        int size1 = clusters.get(1).getPoints().size();
        assertTrue((size0 == 3 && size1 == 3));
    }

    // Tests k equals 1 where all points belong to a single cluster
    @Test
    public void testCluster_singleCluster_returnsSingleClusterWithAllPoints() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(1L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(1.0, 2.0),
            new TestPoint(3.0, 4.0),
            new TestPoint(5.0, 6.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 1, 10);
        assertEquals(1, clusters.size());
        assertEquals(3, clusters.get(0).getPoints().size());
        assertEquals(new TestPoint(3.0, 4.0), clusters.get(0).getCenter());
    }

    // Tests k equals number of points where each point forms its own cluster
    @Test
    public void testCluster_kEqualsNumberOfPoints_eachClusterHasOnePoint() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(1L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(1.0, 1.0),
            new TestPoint(5.0, 5.0),
            new TestPoint(10.0, 10.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 3, 10);
        assertEquals(3, clusters.size());
        for (Cluster<TestPoint> cluster : clusters) {
            assertEquals(1, cluster.getPoints().size());
        }
    }

    // Tests regression for small distance points (< 1.0) where integer distance accumulation causes bug
    @Test
    public void testCluster_smallDistancePoints_correctlyClustersPointsWithFractionalDistances() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(123456L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.01, 0.01),
            new TestPoint(0.02, 0.01),
            new TestPoint(0.01, 0.02),
            new TestPoint(0.50, 0.50),
            new TestPoint(0.51, 0.50),
            new TestPoint(0.50, 0.51)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 2, 20);
        assertEquals(2, clusters.size());
        int count = 0;
        for (Cluster<TestPoint> cluster : clusters) {
            count += cluster.getPoints().size();
        }
        assertEquals(6, count);
        assertTrue(clusters.get(0).getPoints().size() == 3 && clusters.get(1).getPoints().size() == 3);
    }

    // Tests negative maxIterations allows running until convergence
    @Test
    public void testCluster_negativeMaxIterations_runsUntilConvergence() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(42L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(1.0, 1.0),
            new TestPoint(1.0, 2.0),
            new TestPoint(9.0, 8.0),
            new TestPoint(8.0, 9.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 2, -1);
        assertEquals(2, clusters.size());
        int totalPoints = clusters.get(0).getPoints().size() + clusters.get(1).getPoints().size();
        assertEquals(4, totalPoints);
    }

    // Tests maxIterations set to 0 returns initial clusters
    @Test
    public void testCluster_zeroMaxIterations_stopsImmediatelyAfterInit() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(42L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.0, 0.0),
            new TestPoint(10.0, 10.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 2, 0);
        assertEquals(2, clusters.size());
    }

    // Tests LARGEST_POINTS_NUMBER empty cluster strategy
    @Test
    public void testCluster_emptyClusterStrategyLargestPointsNumber_reassignsCorrectly() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(42L),
                KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER);

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.0),
            new TestPoint(1.0),
            new TestPoint(2.0),
            new TestPoint(10.0),
            new TestPoint(11.0),
            new TestPoint(12.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 3, 10);
        assertEquals(3, clusters.size());
        int totalPoints = 0;
        for (Cluster<TestPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals(6, totalPoints);
    }

    // Tests FARTHEST_POINT empty cluster strategy
    @Test
    public void testCluster_emptyClusterStrategyFarthestPoint_reassignsCorrectly() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(42L),
                KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT);

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.0),
            new TestPoint(0.5),
            new TestPoint(10.0),
            new TestPoint(10.5),
            new TestPoint(100.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 3, 10);
        assertEquals(3, clusters.size());
        int totalPoints = 0;
        for (Cluster<TestPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals(5, totalPoints);
    }

    // Tests ERROR empty cluster strategy throwing ConvergenceException when empty cluster occurs
    @Test(expected = ConvergenceException.class)
    public void testCluster_emptyClusterStrategyError_throwsConvergenceException() {
        // Construct points where identical points force duplicate initial centers leading to empty cluster
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(1L),
                KMeansPlusPlusClusterer.EmptyClusterStrategy.ERROR);

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.0, 0.0),
            new TestPoint(0.0, 0.0),
            new TestPoint(0.0, 0.0)
        );

        clusterer.cluster(points, 3, 10);
    }

    // Tests default constructor initializes with LARGEST_VARIANCE
    @Test
    public void testCluster_defaultConstructor_usesLargestVarianceStrategy() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(1L));

        List<TestPoint> points = Arrays.asList(
            new TestPoint(1.0, 1.0),
            new TestPoint(2.0, 2.0),
            new TestPoint(100.0, 100.0),
            new TestPoint(101.0, 101.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 2, 5);
        assertEquals(2, clusters.size());
        for (Cluster<TestPoint> c : clusters) {
            assertEquals(2, c.getPoints().size());
        }
    }

    // Tests LARGEST_VARIANCE empty cluster resolution
    @Test
    public void testCluster_emptyClusterStrategyLargestVariance_reassignsCenter() {
        KMeansPlusPlusClusterer<TestPoint> clusterer =
            new KMeansPlusPlusClusterer<TestPoint>(new Random(123L),
                KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);

        List<TestPoint> points = Arrays.asList(
            new TestPoint(0.0, 0.0),
            new TestPoint(1.0, 0.0),
            new TestPoint(0.0, 1.0),
            new TestPoint(1.0, 1.0),
            new TestPoint(50.0, 50.0),
            new TestPoint(51.0, 50.0),
            new TestPoint(50.0, 51.0),
            new TestPoint(51.0, 51.0)
        );

        List<Cluster<TestPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertEquals(2, clusters.size());
        assertEquals(4, clusters.get(0).getPoints().size());
        assertEquals(4, clusters.get(1).getPoints().size());
    }
}