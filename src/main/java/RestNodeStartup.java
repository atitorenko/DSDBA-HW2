import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ConnectorConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.util.Arrays;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;
import static org.apache.ignite.cache.CacheRebalanceMode.SYNC;
import static org.apache.ignite.cache.CacheWriteSynchronizationMode.FULL_SYNC;
import static org.apache.ignite.configuration.DeploymentMode.SHARED;

/**
 * The server node with http api. With native persistence storage.
 */
public class RestNodeStartup {

    /**
     * Runs ignite node.
     * @param args no required.
     * @throws IgniteException ignite exception.
     */
    public static void main(String[] args) throws IgniteException {
        IgniteConfiguration cfg = getConfig();
        CacheConfiguration<Integer, Citizen> tripsCacheCfg = configureTripsCache(cfg);
        CacheConfiguration<Integer, Citizen> salaryCacheCfg = configureSalaryCache(cfg);
        Ignite ignite = Ignition.start(cfg);
        ignite.active(true);
        IgniteCache<Integer, Citizen> cache1 = ignite.getOrCreateCache(tripsCacheCfg);
        IgniteCache<Integer, Citizen> cache2 = ignite.getOrCreateCache(salaryCacheCfg);
    }

    /**
     * Method for ignite configuration.
     * @return object of ignite config.
     * @throws IgniteException exception.
     */
    static IgniteConfiguration getConfig() throws IgniteException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        configureStorage(cfg);
        configureJetty(cfg);
        configureNetwork(cfg);
        return cfg;
    }

    /**
     * Method for configuration durable memory using strategy.
     *
     * @param cfg ignite configuration.
     */
    private static void configureStorage(IgniteConfiguration cfg) {
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
        cfg.setDataStorageConfiguration(storageCfg);
        cfg.setLocalHost("localhost");
        cfg.setDeploymentMode(SHARED);
        cfg.setPeerClassLoadingEnabled(false);
    }

    /**
     * Jetty configuration needs for realization ignite REST API.
     * @param cfg ignite configuration.
     */
    private static void configureJetty(IgniteConfiguration cfg) {
        ConnectorConfiguration connConf = new ConnectorConfiguration();
        connConf.setJettyPath("/home/cloudera/IdeaProjects/PersistentStore/jetty-conf.xml");
        connConf.setMessageInterceptor(new CitizenInterceptor());
        cfg.setConnectorConfiguration(connConf);
    }

    /**
     * Separate partitioned cache for a data about trip statistics.
     * @param cfg ignite configuration.
     */
    private static CacheConfiguration<Integer, Citizen> configureTripsCache(IgniteConfiguration cfg) {
        CacheConfiguration<Integer, Citizen> tripsCacheCfg = new CacheConfiguration();
        tripsCacheCfg.setName("trips");
        tripsCacheCfg.setAtomicityMode(TRANSACTIONAL);
        tripsCacheCfg.setWriteSynchronizationMode(FULL_SYNC);
        tripsCacheCfg.setRebalanceMode(SYNC);
        tripsCacheCfg.setIndexedTypes(Integer.class, Citizen.class);
        tripsCacheCfg.setCacheMode(CacheMode.PARTITIONED);
        cfg.setCacheConfiguration(tripsCacheCfg);
        return tripsCacheCfg;
    }

    /**
     * Separate partitioned cache for a data about citizens salary statistics.
     * @param cfg ignite configuration.
     */
    private static CacheConfiguration<Integer, Citizen> configureSalaryCache(IgniteConfiguration cfg) {
        CacheConfiguration<Integer, Citizen> salaryCacheCfg = new CacheConfiguration();
        salaryCacheCfg.setName("salary");
        salaryCacheCfg.setAtomicityMode(TRANSACTIONAL);
        salaryCacheCfg.setWriteSynchronizationMode(FULL_SYNC);
        salaryCacheCfg.setRebalanceMode(SYNC);
        salaryCacheCfg.setIndexedTypes(Integer.class, Citizen.class);
        salaryCacheCfg.setCacheMode(CacheMode.PARTITIONED);
        cfg.setCacheConfiguration(salaryCacheCfg);
        return salaryCacheCfg;
    }


    /**
     * Node discovery configuration.
     * @param cfg ignite configuration.
     */
    private static void configureNetwork(IgniteConfiguration cfg) {
        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
        //TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        //ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        //discoSpi.setIpFinder(ipFinder);

        TcpDiscoveryMulticastIpFinder tcMp = new TcpDiscoveryMulticastIpFinder();
        tcMp.setAddresses(Arrays.asList("localhost"));
        discoSpi.setIpFinder(tcMp);
        cfg.setDiscoverySpi(discoSpi);

        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setMessageQueueLimit(1024);
        cfg.setCommunicationSpi(communicationSpi);
    }
}