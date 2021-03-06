/***********************************************************
 Copyright (C) 2011 VeriSign, Inc.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-0107  USA

 http://www.verisign.com/nds/naming/namestore/techdocs.html
 ***********************************************************/

package com.verisign.epp.framework;

import org.apache.log4j.Logger;

import com.verisign.epp.codec.balance.EPPBalanceInfoCmd;
import com.verisign.epp.codec.balance.EPPBalanceMapFactory;
import com.verisign.epp.codec.gen.EPPCodecException;
import com.verisign.epp.codec.gen.EPPFactory;
import com.verisign.epp.codec.gen.EPPMessage;
import com.verisign.epp.util.EPPCatFactory;

/**
 * This class provides an interface for handling EPP Balance Commands in a
 * Server implementation. EPPEvents are handled by the handleEvent() method here
 * and routed to the appropriate abstract member function. Subclasses should
 * override the abstract methods to define the desired behavior of a particular
 * command when it is received. A subclassed instance of
 * {@link EPPBalanceHandler} should be registered with the {@link EPPDispatcher}
 * so that EPPEvents related to the Balance Mapping will be handled there.
 */
public abstract class EPPBalanceHandler implements EPPEventHandler {

	/** Log4j category for logging */
	private static final Logger cat = Logger.getLogger(EPPBalanceHandler.class
			.getName(), EPPCatFactory.getInstance().getFactory());

	/**
	 * Construct an instance of {@link EPPBalanceHandler}. Whenever an
	 * {@link EPPBalanceHandler} instance is created it also adds
	 * {@link EPPBalanceMapFactory} to the {@link EPPFactory} map factory.
	 */
	public EPPBalanceHandler() {
		try {
			EPPFactory.getInstance().addMapFactory(
					EPPBalanceMapFactory.class.getName());
		}
		catch (EPPCodecException e) {
			cat.error("Couldn't load the Map Factory for the Balance Mapping",
					e);
			System.exit(1);
		}
	}

	/**
	 * Returns the Balance Namespace associated with this handler.
	 * 
	 * @return String the Balance Namespace associated with this handler.
	 */
	public String getNamespace() {
		return EPPBalanceMapFactory.NS;
	}

	/**
	 * Returns the {@link EPPEventResponse} after processing the passed in
	 * <code>aEvent</code>. This method receives an {@link EPPEvent} and routes
	 * it to the appropriate abstract method.
	 * 
	 * @param aEvent
	 *            An {@link EPPEvent} that contains the
	 *            {@link com.verisign.epp.codec.gen.EPPCommand}
	 * @param aData
	 *            Any data that a Server needs to send to this
	 *            {@link EPPBalanceHandler} instance.
	 * @return the {@link EPPEventResponse} after processing the passed in
	 *         <code>aEvent</code>.
	 * @exception EPPEventException
	 *                Thrown if an unrecognized {@link EPPEvent} is received
	 */
	public EPPEventResponse handleEvent(EPPEvent aEvent, Object aData)
			throws EPPEventException {

		try {
			this.preHandleEvent(aEvent, aData);
		}
		catch (EPPHandleEventException e) {
			return new EPPEventResponse(e.getResponse());
		}

		EPPMessage message = aEvent.getMessage();
		EPPEventResponse response;

		if (message instanceof EPPBalanceInfoCmd) {
			response = doBalanceInfo(aEvent, aData);
		}
		else {
			throw new EPPEventException(
					"In EPPBalanceHandler an unsupported event was found");
		}

		try {
			this.postHandleEvent(aEvent, aData);
		}
		catch (EPPHandleEventException e) {
			return new EPPEventResponse(e.getResponse());
		}

		return response;
	}

	/**
	 * Handles any common behavior that all Balance commands need to execute
	 * before they execute their command specific behavior. The default
	 * implementation does nothing.
	 * 
	 * @param aEvent
	 *            The {@link EPPEvent} that is being handled
	 * @param aData
	 *            Any data that a Server needs to send to this
	 *            {@link EPPBalanceHandler}
	 * @exception EPPHandleEventException
	 *                Thrown if an error condition occurs. It must contain an
	 *                {@link EPPEventResponse}
	 */
	protected void preHandleEvent(EPPEvent aEvent, Object aData)
			throws EPPHandleEventException {
	}

	/**
	 * Handles any common behavior that all Balance commands need to execute
	 * after they execute their command specific behavior. The default
	 * implementation does nothing
	 * 
	 * @param aEvent
	 *            The {@link EPPEvent} that is being handled
	 * @param aData
	 *            Any data that a Server needs to send to this
	 *            {@link EPPBalanceHandler}
	 * @exception EPPHandleEventException
	 *                Thrown if an error condition occurs. It must contain an
	 *                {@link EPPEventResponse}
	 */
	protected void postHandleEvent(EPPEvent aEvent, Object aData)
			throws EPPHandleEventException {
	}

	/**
	 * Returns the {@link EPPEventResponse} back to the client after processing
	 * the passed in <code>aEvent</code>. Invoked when a Balance Info command is
	 * received. Subclasses should define the behavior when a Balance Info
	 * command is received.
	 * 
	 * @param aEvent
	 *            The {@link EPPEvent} that is being handled
	 * @param aData
	 *            Any data that a Server needs to send to this
	 *            {@link EPPBalanceHandler}
	 * @return the {@link EPPEventResponse} back to the client after processing
	 *         the passed in <code>aEvent</code>.
	 */
	protected abstract EPPEventResponse doBalanceInfo(EPPEvent aEvent,
			Object aData);
}
