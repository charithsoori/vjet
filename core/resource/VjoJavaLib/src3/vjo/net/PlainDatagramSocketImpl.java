package vjo.net;

/*
 * @(#)src/classes/sov/java/net/PlainDatagramSocketImpl.javam, net, asdev, 20070119 1.6
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v5.0
 * (C) Copyright IBM Corp. 1998, 2005. All Rights Reserved
 * ===========================================================================
 */

/*
 * ===========================================================================
 (C) Copyright Sun Microsystems Inc, 1992, 2004. All rights reserved.
 * ===========================================================================
 */

 

/*
 * Change activity:
 *
 * Reason  Date   Origin   Description
 * ------  ------ -------- ---------------------------------------------------
 * 083061  150205 stalleyj Munge this file to make fd fields int/long
 * 087896  310505 belldav  Move case SO_TIMEOUT in getOption
 * 089602  010605 belldav  Correct fd problems and move closer to Sun
 * ===========================================================================
 */

import java.lang.IllegalArgumentException ;

import java.util.Enumeration;

import java.io.IOException;
import java.io.InterruptedIOException;

import vjo.lang.* ;
import vjo.lang.Boolean ;
import vjo.lang.Integer ;

import vjo.io.FileDescriptor;

/**
 * Concrete datagram and multicast socket implementation base class.
 * Note: This is not a public class, so that applets cannot call
 * into the implementation directly and hence cannot bypass the
 * security checks present in the DatagramSocket and MulticastSocket
 * classes.
 *
 * @author Pavani Diwanji
 */

class PlainDatagramSocketImpl extends DatagramSocketImpl
{
    /* timeout value for receive() */
    private int timeout = 0;
    private int trafficClass = 0;
    private boolean connected = false;
    private InetAddress connectedAddress = null;
    private int connectedPort = -1;

    /* cached socket options */
    private int multicastInterface = 0;
    private boolean loopbackMode = true;
    private int ttl = -1;

    /* Used for IPv6 on Windows only */
    private FileDescriptor fd1;

/* saved between peek() and receive() calls */    
//ibm@83061 start



    private int fduse=-1;


    /* saved between successive calls to receive, if data is detected
     * on both sockets at same time. To ensure that one socket is not
     * starved, they rotate using this field
     */



    private int lastfd=-1;
                                    
//ibm@83061 end

    /*
     * Needed for ipv6 on windows because we need to know
     * if the socket was bound to ::0 or 0.0.0.0, when a caller
     * asks for it. In this case, both sockets are used, but we
     * don't know whether the caller requested ::0 or 0.0.0.0
     * and need to remember it here.
     */
    private InetAddress anyLocalBoundAddr=null;

    /**
     * Load net library into runtime.
     */
    static {
	java.security.AccessController.doPrivileged(
		  new sun.security.action.LoadLibraryAction("net"));
	init();
    }

    /**
     * Creates a datagram socket
     */
    protected synchronized void create() throws SocketException {
	fd = new FileDescriptor();
        fd1 = new FileDescriptor();
	datagramSocketCreate();
    }

    /**
     * Binds a datagram socket to a local port.
     */
    protected synchronized void bind(int lport, InetAddress laddr)
        throws SocketException {

        bind0(lport, laddr);
        if (laddr.isAnyLocalAddress()) {
            anyLocalBoundAddr = laddr;
        }
    }

    protected synchronized native void bind0(int lport, InetAddress laddr)
        throws SocketException;

    /**
     * Sends a datagram packet. The packet contains the data and the
     * destination address to send the packet to.
     * @param packet to be sent.
     */
    protected void send(DatagramPacket p) throws IOException        /*ibm@1173*/
    {                                                               /*ibm@1173*/
        send(p,p.getData(),fd,p.getAddress());                      /*ibm@1173*/
    }                                                               /*ibm@1173*/

    private native void send (DatagramPacket p, byte[] b,           /*ibm@1173*/
                              FileDescriptor fd,                    /*ibm@1173*/
	   		      InetAddress addr) throws SocketException;
                                                                    /*ibm@1173*/

    /**
     * Connects a datagram socket to a remote destination. This associates the remote
     * address with the local socket so that datagrams may only be sent to this destination
     * and received from this destination.
     * @param address the remote InetAddress to connect to
     * @param port the remote port number
     */
    protected void connect(InetAddress address, int port) throws SocketException {
	connect0(address, port);
	connectedAddress = address;
	connectedPort = port;
	connected = true;
    }

    /**
     * Disconnects a previously connected socket. Does nothing if the socket was
     * not connected already.
     */
    protected void disconnect() {
	disconnect0(connectedAddress.family);
	connected = false;
	connectedAddress = null;
	connectedPort = -1;
    }

    /**
     * Peek at the packet to see who it is from.
     * @param return the address which the packet came from.
     */
    protected synchronized native int peek(InetAddress i) throws IOException;
    protected synchronized native int peekData(DatagramPacket p) throws IOException;
    /**
     * Receive the datagram packet.
     * @param Packet Received.
     */
    protected synchronized void receive(DatagramPacket p)           /*ibm@1173*/
        throws IOException                                          /*ibm@1173*/
    {                                                               /*ibm@1173*/
        try {
            receive0(p,p.getData(),fd,p.getAddress());                   /*ibm@1173*/
        } finally {
            fduse = -1;
        }
    }                                                               /*ibm@1173*/

    private native void receive0 (DatagramPacket p, byte[] b,        /*ibm@1173*/
                                 FileDescriptor fd,                 /*ibm@1173*/
	   		         InetAddress addr) throws SocketException;
                                                                    /*ibm@1173*/


    /**
     * Set the TTL (time-to-live) option.
     * @param TTL to be set.
     */
    protected native void setTimeToLive(int ttl) throws IOException;

    /**
     * Get the TTL (time-to-live) option.
     */
    protected native int getTimeToLive() throws IOException;

    /**
     * Set the TTL (time-to-live) option.
     * @param TTL to be set.
     */
    protected native void setTTL(byte ttl) throws IOException;

    /**
     * Get the TTL (time-to-live) option.
     */
    protected native byte getTTL() throws IOException;

    /**
     * Join the multicast group.
     * @param multicast address to join.
     */
    protected void join(InetAddress inetaddr) throws IOException {
	join(inetaddr, null);
    }

    /**
     * Leave the multicast group.
     * @param multicast address to leave.
     */
    protected void leave(InetAddress inetaddr) throws IOException {
	leave(inetaddr, null);
    }
    /**
     * Join the multicast group.
     * @param multicast address to join.
     * @param netIf specifies the local interface to receive multicast
     *        datagram packets
     * @throws  IllegalArgumentException if mcastaddr is null or is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */

    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
	throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
	join(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }

    private native void join(InetAddress inetaddr, NetworkInterface netIf)
	throws IOException;

    /**
     * Leave the multicast group.
     * @param multicast address to leave.
     * @param netIf specified the local interface to leave the group at
     * @throws  IllegalArgumentException if mcastaddr is null or is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
	throws IOException {
        if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
	leave(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }

    private native void leave(InetAddress inetaddr, NetworkInterface netIf)
	throws IOException;

    /**
     * Close the socket.
     */
    protected void close() {
	if (fd != null || fd1 != null) {
	    datagramSocketClose();
	    fd = null;
            fd1 = null;
	}
    }

    protected void finalize() {
	close();
    }

    /**
     * set a value - since we only support (setting) binary options
     * here, o must be a Boolean
     */

     public void setOption(int optID, Object o) throws SocketException {
         if (fd == null && fd1 == null) {
            throw new SocketException("Socket Closed");
         }
	 switch (optID) {
	    /* check type safety b4 going native.  These should never
	     * fail, since only java.Socket* has access to
	     * PlainSocketImpl.setOption().
	     */
	 case SO_TIMEOUT:
	     if (o == null || !(o instanceof Integer)) {
		 throw new SocketException("bad argument for SO_TIMEOUT");
	     }
	     int tmp = ((Integer) o).intValue();
	     if (tmp < 0)
		 throw new IllegalArgumentException("timeout < 0");
             timeout = tmp; /*ibm@89602*/
	     return;        /*ibm@89602*/
	 case IP_TOS:
	     if (o == null || !(o instanceof Integer)) {
		 throw new SocketException("bad argument for IP_TOS");
	     }
	     trafficClass = ((Integer)o).intValue();
	     break;
	 case SO_REUSEADDR:
	     if (o == null || !(o instanceof Boolean)) {
		 throw new SocketException("bad argument for SO_REUSEADDR");
	     }
	     break;
	 case SO_BROADCAST:
	     if (o == null || !(o instanceof Boolean)) {
		 throw new SocketException("bad argument for SO_BROADCAST");
	     }
	     break;
	 case SO_BINDADDR:
	     throw new SocketException("Cannot re-bind Socket");
	 case SO_RCVBUF:
	 case SO_SNDBUF:
	     if (o == null || !(o instanceof Integer) ||
		 ((Integer)o).intValue() < 0) {
		 throw new SocketException("bad argument for SO_SNDBUF or " +
					   "SO_RCVBUF");
	     }
	     break;
	 case IP_MULTICAST_IF:
	     if (o == null || !(o instanceof InetAddress))
		 throw new SocketException("bad argument for IP_MULTICAST_IF");
	     break;
	 case IP_MULTICAST_IF2:
	     if (o == null || !(o instanceof NetworkInterface))
		 throw new SocketException("bad argument for IP_MULTICAST_IF2");
	     break;
	 case IP_MULTICAST_LOOP:
	     if (o == null || !(o instanceof Boolean))
		 throw new SocketException("bad argument for IP_MULTICAST_LOOP");
	     break;
	 default:
	     throw new SocketException("invalid option: " + optID);
	 }
	 socketSetOption(optID, o);
     }

    /*
     * get option's state - set or not
     */

    public Object getOption(int optID) throws SocketException {
        if (fd == null && fd1 == null) {
            throw new SocketException("Socket Closed");
        }

	Object result;

	switch (optID) {
            case SO_TIMEOUT:
                result = new Integer(timeout);
                break;
	    case IP_TOS:
		result = socketGetOption(optID);
		if ( ((Integer)result).intValue() == -1) {
		    result = new Integer(trafficClass);
		}
		break;
            case SO_BINDADDR:
                if (fd != null && fd1 != null) {
                    return anyLocalBoundAddr;
                }
                /* fall through */
	    case IP_MULTICAST_IF:
	    case IP_MULTICAST_IF2:
	    case SO_RCVBUF:
	    case SO_SNDBUF:
	    case IP_MULTICAST_LOOP:
	    case SO_REUSEADDR:
	    case SO_BROADCAST:
		result = socketGetOption(optID);
		break;

	    default:
		throw new SocketException("invalid option: " + optID);
  	}

	return result;
    }

    private native void datagramSocketCreate() throws SocketException;
    private native void datagramSocketClose();
    private native void socketSetOption(int opt, Object val)
        throws SocketException;
    private native Object socketGetOption(int opt) throws SocketException;

    private native void connect0(InetAddress address, int port) throws SocketException;
    private native void disconnect0(int family);

    /**
     * Perform class load-time initializations.
     */
    private native static void init();

}


