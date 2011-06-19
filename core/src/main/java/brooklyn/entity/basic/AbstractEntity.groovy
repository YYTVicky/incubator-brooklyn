
package brooklyn.entity.basic

import java.util.concurrent.CopyOnWriteArrayList

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import brooklyn.entity.Application
import brooklyn.entity.Entity
import brooklyn.entity.EntityClass
import brooklyn.entity.EntitySummary
import brooklyn.entity.Group
import brooklyn.event.Event
import brooklyn.event.EventListener
import brooklyn.event.Sensor
import brooklyn.management.ManagementContext;
import brooklyn.event.basic.AttributeMap
import brooklyn.location.Location
import brooklyn.util.internal.LanguageUtils

/**
 * Default {@link Entity} definition.
 * 
 * Provides several common fields ({@link #name}, {@link #id});
 * also provides a map {@link #config} which contains arbitrary fields.
 * <p>
 * Fields in config can be accessed (get and set) without referring to config,
 * (through use of propertyMissing). Note that config is typically inherited
 * by children, whereas the fields are not.
 *
 * @author alex
 */
public abstract class AbstractEntity implements Entity {
    static final Logger log = LoggerFactory.getLogger(Entity.class);
 
    String id = LanguageUtils.newUid();
    Map<String,Object> presentationAttributes = [:]
    
    String displayName;
    
    /**
     * Properties can be accessed or set on the entity itself; can also be accessed
     * from ancestors if not present on an entity
     */
    Collection<Location> locations = []
 
    // TODO ref to local mgmt context and sub mgr etc
 
    protected final AttributeMap attributesInternal = new AttributeMap(this)

    public void propertyMissing(String name, value) { attributes[name] = value }
 
    public Object propertyMissing(String name) {
        if (attributes.containsKey(name)) return attributes[name];
        else {
            //TODO could be more efficient ;)
            def v = null
            if (parents.find { parent -> v = parent.attributes[name] }) return v;
        }
        log.debug "no property $name on $this"
    }
	
    /** Entity hierarchy */
    final Collection<Group> parents = new CopyOnWriteArrayList<Group>()
 
    Application application

    /**
     * Adds a parent, registers with application if necessary
     */
    public void addParent(Group e) {
        parents.add e
        getApplication()
    }
	public String getParentId() {
		parents ? parents[0].id : null
	}
	public Collection<String> getGroupIds() {
		parents.collect { it.id }
	}

    /**
     * Returns the application, looking it up if not yet known (registering if necessary)
     */
    public Application getApplication() {
        if (application!=null) return application;
        def app = parents.find({ it.getApplication() })?.getApplication()
        if (app) {
            registerWithApplication(app)
            application
        }
        app
    }
	public String getApplicationId() {
		getApplication()?.id
	}

	public ManagementContext getManagementContext() {
		getApplication()?.getManagementContext()
	}
	
    protected synchronized void registerWithApplication(Application app) {
        if (application) return;
        this.application = app
        app.registerEntity(this)
    }

    public EntitySummary getImmutableSummary() {
        return new BasicEntitySummary(this)
    }
    
    public EntityClass getEntityClass() {
		//TODO registry? or a transient?
		new BasicEntityClass(getClass())
    }
    
    /**
     * Should be invoked at end-of-life to clean up the item.
     */
    public void destroy() {
		//FIXME this doesn't exist, but we need some way of deleting stale items
        removeApplicationRegistrant()
    }

    /** default toString is simplified name of class, together with selected arguments */
    @Override
    public String toString() {
        StringBuffer result = []
        result << getClass().getSimpleName()
        if (!result) result << getClass().getName()
        //TODO groovy 1.8, use collectEntries
        result << toStringFieldsToInclude().collect({
            def v = this.hasProperty(it) ? this[it] : this.properties[it]
            v ? "$it=$v" : null
        }).findAll { it }
        result
    }
 
    /** override this, adding to the collection, to supply fields whose value, if not null, should be included in the toString */
    public Collection<String> toStringFieldsToInclude() { ['id', 'displayName'] }

    public AbstractEntity(Map flags=[:]) {
        def parent = flags.remove('parent')

        //place named-arguments into corresponding fields if they exist, otherwise put into attributes map
        this.attributes << LanguageUtils.setFieldsFromMap(this, flags)

        //set the parent if supplied; accept as argument or field
        if (parent) parent.addChild(this)
    }

    Map<String,Object> getAttributes() {
        return attributesInternal.asMap();
    }
    
    public <T> void updateAttribute(Sensor<T> attribute, T val) {
        attributesInternal.update(attribute, val);
    }
    
    // TODO implement private methods
    // private void subscribe(EventFilter filter, EventListener listener) { }
    // private void subscribe(Predicate<Entity> entities, EventFilter filter, EventListener listener) { }
    
    /** @see Entity#subscribe(String, String, EventListener) */
    public <T> void subscribe(String entityId, String sensorname, EventListener<T> listener) {
        // TODO complete
    }
     
    /** @see Entity#raiseEvent(Event) */
    public <T> void raiseEvent(Event<T> event) {
        // TODO complete
    }
}
