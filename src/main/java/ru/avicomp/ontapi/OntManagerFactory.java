package ru.avicomp.ontapi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.semanticweb.owlapi.OWLAPIParsersModule;
import org.semanticweb.owlapi.OWLAPIServiceLoaderModule;
import org.semanticweb.owlapi.annotations.OwlapiModule;
import org.semanticweb.owlapi.model.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import uk.ac.manchester.cs.owl.owlapi.CompressionEnabled;
import uk.ac.manchester.cs.owl.owlapi.OWLAPIImplModule;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;
import uk.ac.manchester.cs.owl.owlapi.concurrent.NoOpReadWriteLock;
import uk.ac.manchester.cs.owl.owlapi.concurrent.NonConcurrentDelegate;

/**
 * see {@link org.semanticweb.owlapi.apibinding.OWLManager}
 * <p>
 * Created by @szuev on 27.09.2016.
 */
public class OntManagerFactory implements OWLOntologyManagerFactory {

    private static final ONTManagerProfile DEFAULT_PROFILE = new ONTManagerProfile();

    private static ManagerProfile profile = DEFAULT_PROFILE;

    public static OWLDataFactory getDataFactory() {
        return DEFAULT_PROFILE.getOWLDataFactory();
    }

    public static OntologyManager createONTManager() {
        return DEFAULT_PROFILE.createManager();
    }

    public static OntologyManager createONTConcurrentManager() {
        return new ONTManagerProfile(true).createManager();
    }

    public static OWLOntologyManager createOWLManager() {
        return new OWLManagerProfile().createManager();
    }

    public static OWLOntologyManager createOWLConcurrentManager() {
        return new OWLConcurrentManagerProfile().createManager();
    }

    public static ManagerProfile getProfile() {
        return profile;
    }

    public static void setProfile(ManagerProfile p) {
        profile = OntApiException.notNull(p, "Null manager profile specified.");
    }

    @Override
    public OWLOntologyManager get() {
        return profile.createManager();
    }

    /**
     * copy-past from {@link OWLAPIImplModule}
     * TODO: we don't need any injections. replace them with direct calls.
     *
     * Created by @szuev on 27.09.2016.
     */
    @OwlapiModule
    public static class ONTImplModule extends AbstractModule {
        private boolean concurrent;

        ONTImplModule(boolean b) {
            concurrent = b;
        }

        @Override
        protected void configure() {
            bind(ReadWriteLock.class).to(concurrent ? ReentrantReadWriteLock.class : NoOpReadWriteLock.class).asEagerSingleton();
            bind(boolean.class).annotatedWith(CompressionEnabled.class).toInstance(Boolean.FALSE);
            bind(OWLDataFactory.class).to(OWLDataFactoryImpl.class).asEagerSingleton();
            bind(OWLOntologyManager.class).to(OntologyManagerImpl.class).asEagerSingleton();
            bind(OntologyManager.class).to(OntologyManagerImpl.class).asEagerSingleton();
            bind(OntologyManager.class).annotatedWith(NonConcurrentDelegate.class).to(OntologyManagerImpl.class).asEagerSingleton();
            bind(OWLOntologyBuilder.class).to(ONTBuilder.class); //<--todo
            bind(OWLOntologyBuilder.class).annotatedWith(NonConcurrentDelegate.class).to(ONTBuilder.class);
            install(new FactoryModuleBuilder().implement(OntologyModel.class, OntologyModelImpl.class).build(ONTImplementationFactory.class));
            multibind(OWLOntologyFactory.class, OntBuildingFactoryImpl.class);
        }

        @SafeVarargs
        protected final <T> Multibinder<T> multibind(Class<T> type, Class<? extends T>... implementations) {
            Multibinder<T> binder = Multibinder.newSetBinder(binder(), type);
            for (Class<? extends T> i : implementations) {
                binder.addBinding().to(i);
            }
            return binder;
        }
    }

    private static class ONTBuilder implements OWLOntologyBuilder {
        private final transient ONTImplementationFactory implementationFactory;

        @Inject
        public ONTBuilder(ONTImplementationFactory implementationFactory) {
            this.implementationFactory = OntApiException.notNull(implementationFactory);
        }

        @Override
        public OntologyModel createOWLOntology(@Nonnull OWLOntologyManager manager, @Nonnull OWLOntologyID ontologyID) {
            return implementationFactory.createOWLOntology((OntologyManager) manager, ontologyID);
        }
    }

    @FunctionalInterface
    interface ONTImplementationFactory extends Serializable {
        OntologyModel createOWLOntology(OntologyManager manager, OWLOntologyID ontologyID);
    }

    public interface ManagerProfile<M extends OWLOntologyManager> {
        M createManager();
    }

    public static abstract class BaseManagerProfile {
        public abstract Injector createInjector();

        public OWLDataFactory getOWLDataFactory() {
            return createInjector().getInstance(OWLDataFactory.class);
        }

        public OWLOntologyManager createManager() {
            Injector injector = createInjector();
            OWLOntologyManager instance = injector.getInstance(OWLOntologyManager.class);
            injector.injectMembers(instance);
            return instance;
        }
    }

    public static class ONTManagerProfile extends BaseManagerProfile implements ManagerProfile<OntologyManager> {
        private final boolean concurrent;

        public ONTManagerProfile() {
            this(false);
        }

        public ONTManagerProfile(boolean concurrent) {
            this.concurrent = concurrent;
        }

        @Override
        public Injector createInjector() {
            return Guice.createInjector(new ONTImplModule(concurrent), new OWLAPIParsersModule(),
                    new OWLAPIServiceLoaderModule());
        }

        @Override
        public OntologyManager createManager() {
            return (OntologyManager) super.createManager();
        }
    }

    public static class OWLManagerProfile extends BaseManagerProfile implements ManagerProfile<OWLOntologyManager> {
        @Override
        public Injector createInjector() {
            return Guice.createInjector(new OWLAPIImplModule(Concurrency.NON_CONCURRENT), new OWLAPIParsersModule(),
                    new OWLAPIServiceLoaderModule());
        }
    }

    public static class OWLConcurrentManagerProfile extends BaseManagerProfile implements ManagerProfile<OWLOntologyManager> {
        @Override
        public Injector createInjector() {
            return Guice.createInjector(new OWLAPIImplModule(Concurrency.CONCURRENT), new OWLAPIParsersModule(),
                    new OWLAPIServiceLoaderModule());
        }
    }
}