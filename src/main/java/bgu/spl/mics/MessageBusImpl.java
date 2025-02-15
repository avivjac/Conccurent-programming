package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 *
 * @INVARIANT: 
 * 1. the MessageBusImpl class is a singleton and can only have one instance.
 * 2. all queues for the Microservices are stored and managed in a thread-safe manner.
 * 3. all MicroServices must be registered before they can receive messages.
 * 4. the message queue for each MicroService should be accessible and managed individually for each MicroService.
 * 5. no messages should be lost or skipped for any MicroService, and all messages are handled in an orderly manner.
 * 6. The class is a Thread-Safe Singleton. 
 */

public class MessageBusImpl implements MessageBus {

	// Singleton holder
	private static class MessageBusHolder {
		private static final MessageBusImpl instance = new MessageBusImpl();
	}

	// Subscription maps for events and broadcasts
	private ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscribers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers = new ConcurrentHashMap<>();

	// Message queues for microservices
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueues = new ConcurrentHashMap<>();

	// Map to store Future objects associated with Events
    private ConcurrentHashMap<Event<?>, Future<?>> eventFutureMap = new ConcurrentHashMap<>();

	// Private constructor for singleton
	private MessageBusImpl() {
		this.eventSubscribers = new ConcurrentHashMap<>();
		this.broadcastSubscribers = new ConcurrentHashMap<>();
		this.microServiceQueues = new ConcurrentHashMap<>();
		this.eventFutureMap = new ConcurrentHashMap<>();

		 // Initialize the maps
		this.eventSubscribers.put(PoseEvent.class, new ConcurrentLinkedQueue<>());
		this.eventSubscribers.put(DetectedObjectsEvent.class, new ConcurrentLinkedQueue<>());
		this.eventSubscribers.put(TrackedObjectsEvent.class, new ConcurrentLinkedQueue<>());
		this.broadcastSubscribers.put(TickBroadcast.class, new ConcurrentLinkedQueue<>());
		this.broadcastSubscribers.put(TerminatedBroadcast.class, new ConcurrentLinkedQueue<>());
		this.broadcastSubscribers.put(CrashedBroadcast.class, new ConcurrentLinkedQueue<>());	
	}

	// Singleton instance getter
	public static MessageBusImpl getInstance() {
		return MessageBusHolder.instance;
	}
	/**
     * Method to subscribe a MicroService to an event type
	 * 
     * @PRE-CONDITION:
     * 1. The MicroService has been registered.
     * 2. The event class to be subscribed to is valid.
	 * 
     * @POST-CONDITION:
     * 1. The MicroService is added to the currect map of subscribers for the specified event type.
     */

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
	}
		/**
     * Method to subscribe a MicroService to a broadcast type
     * 
	 * @PRE-CONDITION:
     * 1. The MicroService has been registered.
     * 2. The broadcast class to be subscribed to is valid.
     * 
	 * @POST-CONDITION:
     * 1. The MicroService is added to the currrect map of subscribers for the specified broadcast type.
     */

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
	}

    @SuppressWarnings("unchecked")
	public <T> Future<T> getFuture(Event<T> event) {
        return (Future<T>) eventFutureMap.get(event);
    }
	/**
     * Method to complete an event with the result
	 * 
     * @PRE-CONDITION:
     * 1. The event has been successfully processed by a MicroService.
     * 2. The event is still valid and has not been completed previously.
     * 
	 * @POST-CONDITION:
     * 1. The Future<T> object associated with the event is resolved with the result.
	 * 2. The event is removed from the map of events and futures.
	 * 
     */

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = getFuture(e);
		if (future != null) {
            future.resolve(result);
			this.eventFutureMap.remove(e);
        }
	}
	/**
     * Method to send a broadcast message to all subscribers
	 * 
     * @PRE-CONDITION:
     * 1. all the MicroServices that need to be reached are registered.
     * 2. The message is a valid broadcast.
	 * 
     * @POST-CONDITION:
     * 1. The broadcast message is added to the queues of all MicroServices subscribed to the specific message type.
     * 2. The queues are updated to reflect all subscribers.
     */

	@Override
	public synchronized void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
		if (subscribers != null) {
			for (MicroService m : subscribers) {
				try {
						if (microServiceQueues.get(m) != null) {
							microServiceQueues.get(m).put(b); // Add to message queue
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	/**
     * Method to send an event to a MicroService
	 * 
     * @PRE-CONDITION:
     * 1. The event is a valid and not null.
     * 2. At least one MicroService is registered and subscribed to the event type.
     * 3. The event queue for the MicroService is not full or blocked.
	 * 
     * @POST-CONDITION:
     * 1. The event is sent to the appropriate MicroService's queue in a round-robin fashion.
     * 2. If no appropriate MicroService is found to handle the event, null is returned.
     */

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
        MicroService nextService = null;
        ConcurrentLinkedQueue<MicroService> subscribers = eventSubscribers.get(e.getClass());
        if (subscribers != null && !subscribers.isEmpty()) {
            nextService = subscribers.poll();
            if (nextService != null) {
                subscribers.add(nextService);
            }
        }
        if (nextService != null) {
            Future<T> future = new Future<>();
            this.eventFutureMap.put(e, future); // Add the future and the event to the map
            microServiceQueues.get(nextService).add(e);
            return future;
        }
        return null;
    }

		/**
     * Method to register a MicroService
	 * 
     * @PRE-CONDITION: 
     * 1. The MicroService has not been previously registered.
	 * 
     * @POST-CONDITION: 
     * 1. A new queue is created for the MicroService.
     */

	@Override
	public void register(MicroService m) {
		microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>()); // Create message queue for the microservice
	}
		/**
     * Method to unregister a MicroService
	 * 
     * @PRE-CONDITION:
     * 1. The MicroService has been registered.
     * 2. The MicroService should not have any active messages being processed.
	 * 
     * @POST-CONDITION:
     * 1. The MicroService is removed from the list of registered MicroServices.
     * 2. The MicroService is removed from all event and broadcast subscriptions.
     */

	@Override
	public void unregister(MicroService m) {
		microServiceQueues.remove(m);
		// Remove from event and broadcast subscriptions
		for (ConcurrentLinkedQueue<MicroService> subscribers : eventSubscribers.values()) {
			subscribers.remove(m);
		}
		for (ConcurrentLinkedQueue<MicroService> subscribers : broadcastSubscribers.values()) {
			subscribers.remove(m);
		}
	}
	/**
     * Method to await a message for a specific MicroService
	 * 
     * @PRE-CONDITION:
     * 1. The MicroService is registered with the MessageBusImpl.
	 * 
     * @POST-CONDITION:
     * 1. The MicroService either receives a new message or waits until a message is available.
     * 2. The method is blocking and will resume once a message is available in the queue.
     */

	@Override
	public  Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = microServiceQueues.get(m);
		if (queue == null) {
			throw new IllegalStateException("Microservice is not registered");
		}
		return queue.take(); // Blocking call until a message is available
	}

	//getters-used only for testing
	public ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> getEventSubscribers() {
		return eventSubscribers;
	}
	public ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> getBroadcastSubscribers() {
		return broadcastSubscribers;
	}
	public ConcurrentHashMap<MicroService, BlockingQueue<Message>> getMicroServiceQueues() {
		return microServiceQueues;
	}
	public ConcurrentHashMap<Event<?>, Future<?>> getEventFutureMap() {
		return eventFutureMap;
	}

}