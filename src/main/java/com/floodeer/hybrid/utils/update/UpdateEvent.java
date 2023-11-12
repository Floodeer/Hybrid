package com.floodeer.hybrid.utils.update;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final UpdateType type;

    public UpdateEvent(UpdateType paramUpdateType) {
        this.type = paramUpdateType;
    }

    public UpdateType getType() {
        return this.type;
    }

    public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}